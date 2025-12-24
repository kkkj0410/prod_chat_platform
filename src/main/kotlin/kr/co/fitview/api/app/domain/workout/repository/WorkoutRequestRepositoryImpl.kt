package kr.co.fitview.api.app.domain.workout.repository

import com.querydsl.core.types.Projections
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import kr.co.fitview.api.app.domain.chat.entity.QChatMessage.chatMessage
import kr.co.fitview.api.app.domain.chat.entity.QChatRoom.chatRoom
import kr.co.fitview.api.app.domain.member.entity.QMember
import kr.co.fitview.api.app.domain.review.entity.QReview
import kr.co.fitview.api.app.domain.review.entity.QReview.review
import kr.co.fitview.api.app.domain.workout.condition.AdminWorkoutRequestCondition
import kr.co.fitview.api.app.domain.workout.dto.response.*
import kr.co.fitview.api.app.domain.workout.dto.response.enums.WorkoutRequestStatusForResponse
import kr.co.fitview.api.app.domain.workout.entity.QWorkoutRequest.workoutRequest
import kr.co.fitview.api.app.domain.workout.entity.QWorkoutRequestLog.workoutRequestLog
import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequest
import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import kr.co.fitview.api.app.domain.workout_history.entity.QWorkoutHistory.workoutHistory
import kr.co.fitview.api.app.domain.workout_partner.entity.QWorkoutPartner.workoutPartner
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.global.GlobalErrorCode
import kr.co.fitview.api.app.global.time.Time
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import java.sql.Timestamp
import java.time.LocalDateTime

class WorkoutRequestRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
    private val em: EntityManager,
    private val time: Time
) : WorkoutRequestRepositoryCustom {

    override fun findRecentWorkoutRequest(chatRoomIds: List<Long>): List<LastWorkoutRequestMessage> {

        val inClause = chatRoomIds.joinToString(",")
        val sql = """
        WITH RankedRequests AS (
            SELECT
                wr.workout_request_id,
                wr.status,
                wr.requested_at,
                wr.scheduled_at,
                cm.chat_room_id,
                ROW_NUMBER() OVER (
                    PARTITION BY cm.chat_room_id
                    ORDER BY cm.created_at DESC
                ) AS rn
            FROM workout_request wr
            JOIN chat_message cm
                ON cm.chat_message_id = wr.chat_message_id
            WHERE cm.chat_room_id IN ($inClause)
        )
        SELECT 
        r.workout_request_id,
        r.status,
        r.requested_at,
        r.scheduled_at,
        r.chat_room_id,
        wh.workout_history_id
        FROM RankedRequests r
        LEFT JOIN workout_history wh
        ON wh.workout_request_id = r.workout_request_id
        WHERE rn = 1;
    """.trimIndent()

        val query = em
            .createNativeQuery(sql)

        @Suppress("UNCHECKED_CAST")
        val result = query.resultList as List<Array<Any?>>


        return result.map { r ->
            val workoutRequestId = r[0] as Long
            val dbStatusStr = r[1] as String?
            val dbStatus = dbStatusStr?.let { WorkoutRequestStatus.valueOf(it) }
            val requestedAt = (r[2] as Timestamp).toLocalDateTime()
            val scheduledAt = (r[3] as Timestamp).toLocalDateTime()
            val chatRoomId = r[4] as Long
            val workoutHistoryId = r[5] as Long?

            LastWorkoutRequestMessage(
                workoutRequestId = workoutRequestId,
                status = WorkoutRequestStatusForResponse.from(
                    dbStatus!!,
                    requestedAt,
                    scheduledAt,
                    time.nowLocalDateTime
                ),
                chatRoomId = chatRoomId,
                workoutHistoryId = workoutHistoryId
            )
        }
    }

    override fun findRecentWorkoutRequestEntity(chatRoomId: Long): WorkoutRequest? {
        return queryFactory
            .selectFrom(workoutRequest)
            .join(workoutRequest.chatMessage, chatMessage)
            .where(
                chatMessage.chatRoom.id.eq(chatRoomId),
                workoutRequest.deletedAt.isNull,
            )
            .orderBy(workoutRequest.requestedAt.desc())
            .limit(1)
            .fetchOne()

    }

    override fun findAllPendingWorkoutRequestAlreadyExpire(): List<WorkoutRequestUpdateResponse> {


        val expireByScheduledAt = queryFactory
            .select(
                Projections.constructor(
                    WorkoutRequestUpdateResponse::class.java,
                    chatMessage.chatRoom.id,
                    workoutRequest.id,
                    workoutRequest.status,
                    workoutRequest.fromMember.id,
                    workoutRequest.toMember.id
                )
            )
            .from(workoutRequest)
            .join(workoutRequest.chatMessage, chatMessage)
            .where(
                workoutRequest.status.eq(WorkoutRequestStatus.PENDING),
                workoutRequest.deletedAt.isNull,
                workoutRequest.scheduledAt.lt(time.nowLocalDateTime),
            )
            .fetch()

        val expireByRequestedAt = queryFactory
            .select(
                Projections.constructor(
                    WorkoutRequestUpdateResponse::class.java,
                    chatMessage.chatRoom.id,
                    workoutRequest.id,
                    workoutRequest.status,
                    workoutRequest.fromMember.id,
                    workoutRequest.toMember.id
                )
            )
            .from(workoutRequest)
            .join(workoutRequest.chatMessage, chatMessage)
            .where(
                workoutRequest.status.eq(WorkoutRequestStatus.PENDING),
                workoutRequest.deletedAt.isNull,
                workoutRequest.requestedAt.loe(time.nowLocalDateTime.minusHours(24)),
            )
            .fetch()

        return (expireByScheduledAt + expireByRequestedAt)
            .distinctBy { it.workoutRequestId }
    }

    override fun updateExpireByIdIn(workoutRequestIds: List<Long>) {
        if (workoutRequestIds.isEmpty()) return

        queryFactory
            .update(workoutRequest)
            .set(workoutRequest.status, WorkoutRequestStatus.EXPIRE)
            .where(workoutRequest.id.`in`(workoutRequestIds))
            .execute()

        em.flush()
        em.clear()
    }

    override fun findAllWorkoutRequestBy(condition: AdminWorkoutRequestCondition): Slice<AdminWorkoutRequestResponse> {

        val fromMember = QMember("fromMember")
        val toMember = QMember("toMember")

        val fromMemberHasReview = QReview("fromReview")
        val toMemberHasReview = QReview("toReview")

        val findWorkoutRequests = queryFactory
            .select(
                Projections.constructor(
                    AdminWorkoutRequestResponse::class.java,
                    fromMember.id,
                    toMember.id,
                    workoutRequest.id,
                    fromMember.nickname,
                    toMember.nickname,
                    workoutRequest.status,
                    workoutRequest.requestedAt,
                    workoutRequest.scheduledAt,
                    workoutRequest.location,
                    fromMemberHasReview.id.isNotNull,
                    toMemberHasReview.id.isNotNull
                )
            )
            .from(workoutRequest)
            .join(workoutRequest.fromMember, fromMember)
            .join(workoutRequest.toMember, toMember)
            .leftJoin(workoutHistory)
                .on(workoutHistory.workoutRequest.id.eq(workoutRequest.id))
            .leftJoin(fromMemberHasReview)
                .on(
                    fromMemberHasReview.workoutHistory.id.eq(workoutHistory.id),
                    fromMemberHasReview.fromMember.id.eq(fromMember.id),
                    fromMemberHasReview.toMember.id.eq(toMember.id)
                )
            .leftJoin(toMemberHasReview)
                .on(
                    toMemberHasReview.workoutHistory.id.eq(workoutHistory.id),
                    toMemberHasReview.fromMember.id.eq(toMember.id),
                    toMemberHasReview.toMember.id.eq(fromMember.id)
                )
            .where(
                workoutRequest.deletedAt.isNull,
                condition.workoutRequestId?.let { workoutRequest.id.lt(it) }
            )
            .orderBy(workoutRequest.id.desc())
            .limit(condition.size + 1L)
            .fetch()

        val hasNext = findWorkoutRequests.size > condition.size
        val content = if (hasNext) findWorkoutRequests.subList(0, condition.size) else findWorkoutRequests

        val requestPairs = content.map {
            val small = minOf(it.fromMemberId, it.toMemberId)
            val large = maxOf(it.fromMemberId, it.toMemberId)
            small to large
        }

        val workoutPartnerTriples  =
            queryFactory
                .select(
                    workoutPartner.memberOne.id,
                    workoutPartner.memberTwo.id,
                    workoutPartner.id
                )
                .from(workoutPartner)
                .where(
                    workoutPartner.memberOne.id.`in`(requestPairs.map { it.first }),
                    workoutPartner.memberTwo.id.`in`(requestPairs.map { it.second }),
                )
                .fetch()
                .map { tuple ->
                    val memberOneId = tuple.get(workoutPartner.memberOne.id) as Long
                    val memberTwoId = tuple.get(workoutPartner.memberTwo.id) as Long
                    val wpId = tuple.get(workoutPartner.id) as Long

                    val (small, large) = minOf(memberOneId, memberTwoId) to maxOf(memberOneId, memberTwoId)
                    Triple(small, large, wpId)
                }
                .toSet()

        val workoutRequestIds = findWorkoutRequests.map{it.workoutRequestId}

        val findWorkoutRequestLogs = queryFactory
            .select(
                workoutRequestLog.workoutRequest.id,
                workoutRequestLog.loggedAt.max()
            )
            .from(workoutRequestLog)
            .where(
                workoutRequestLog.workoutRequest.id.`in`(workoutRequestIds)
            )
            .groupBy(workoutRequestLog.workoutRequest.id)
            .fetch()

        val workoutPartnerMap = workoutPartnerTriples.associate {
            (it.first to it.second) to it.third
        }

        val workoutRequestLogMap = findWorkoutRequestLogs.associate { tuple ->
            val requestId = tuple.get(workoutRequestLog.workoutRequest.id)
            val latestLoggedAt = tuple.get(workoutRequestLog.loggedAt.max())
            requestId to latestLoggedAt
        }

        val contentWithExtras = findWorkoutRequests.map { request ->
            val small = minOf(request.fromMemberId, request.toMemberId)
            val large = maxOf(request.fromMemberId, request.toMemberId)

            val wpId = workoutPartnerMap[small to large] ?: 0L
            val latestLoggedAt = workoutRequestLogMap[request.workoutRequestId]

            request.copy(
                workoutPartnerId = wpId,
                respondedAt = latestLoggedAt
            )
        }


        return SliceImpl(contentWithExtras, PageRequest.of(0, condition.size), hasNext)
    }


    override fun findWorkoutRequestByIdAndDeletedAtIsNullWithChatMessage(workoutRequestId: Long): WorkoutRequest? {
        return queryFactory
            .selectFrom(workoutRequest)
            .join(workoutRequest.chatMessage, chatMessage).fetchJoin()
            .where(
                workoutRequest.id.eq(workoutRequestId),
                workoutRequest.deletedAt.isNull
            )
            .fetchOne()
    }

    override fun findWorkoutRequestDetailBy(workoutRequestId: Long): AdminDetailWorkoutRequestResponse {
        val baseRequest = queryFactory
            .select(
                workoutRequest.id,
                workoutRequest.scheduledAt,
                workoutRequest.location,
                workoutRequest.fromMember.id,
                workoutRequest.toMember.id
            )
            .from(workoutRequest)
            .where(workoutRequest.id.eq(workoutRequestId))
            .fetchOne()
            ?: throw GlobalException(GlobalErrorCode.ENTITY_NOT_FOUND)

        val fromMemberId = baseRequest.get(workoutRequest.fromMember.id)!!
        val toMemberId = baseRequest.get(workoutRequest.toMember.id)!!

        val minMemberId = minOf(fromMemberId, toMemberId)
        val maxMemberId = maxOf(fromMemberId, toMemberId)

        val workoutPartnerId = queryFactory
            .select(workoutPartner.id)
            .from(workoutPartner)
            .where(
                workoutPartner.memberOne.id.eq(minMemberId),
                workoutPartner.memberTwo.id.eq(maxMemberId)
            )
            .fetchOne()
            ?: throw GlobalException(GlobalErrorCode.ENTITY_NOT_FOUND)

        val base = AdminDetailWorkoutRequestResponse(
            workoutPartnerId = workoutPartnerId,
            workoutRequestId = baseRequest.get(workoutRequest.id)!!,
            scheduledAt = baseRequest.get(workoutRequest.scheduledAt)!!,
            location = baseRequest.get(workoutRequest.location)!!
        )

        val fromMember = QMember("fromMember")
        val toMember = QMember("toMember")

        val logs = queryFactory
            .select(
                Projections.constructor(
                    AdminDetailWorkoutRequestLogResponse::class.java,
                    workoutRequestLog.status,
                    workoutRequestLog.loggedAt,
                    fromMember.nickname,
                    toMember.nickname
                )
            )
            .from(workoutRequestLog)
            .join(workoutRequestLog.workoutRequest, workoutRequest)
            .join(workoutRequest.fromMember, fromMember)
            .join(workoutRequest.toMember, toMember)
            .where(workoutRequest.id.eq(workoutRequestId))
            .orderBy(workoutRequestLog.loggedAt.asc())
            .fetch()

        val reviewFrom = QMember("reviewFrom")
        val reviewTo = QMember("reviewTo")

        val reviews = queryFactory
            .select(
                Projections.constructor(
                    AdminDetailReviewResponse::class.java,
                    reviewFrom.nickname,
                    reviewTo.nickname,
                    review.postedAt
                )
            )
            .from(review)
            .join(review.fromMember, reviewFrom)
            .join(review.toMember, reviewTo)
            .join(review.workoutHistory, workoutHistory)
            .join(workoutHistory.workoutRequest, workoutRequest)
            .where(workoutRequest.id.eq(workoutRequestId))
            .orderBy(review.postedAt.asc())
            .fetch()

        return base.copy(
            workoutRequestLogs = logs,
            reviews = reviews
        )
    }


}