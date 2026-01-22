package kr.co.fitview.api.app.domain.workout_partner.repository

import com.querydsl.core.types.Projections
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.CaseBuilder
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import kr.co.fitview.api.app.domain.chat.entity.QChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.QChatRoom.chatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.image.entity.QImage.image
import kr.co.fitview.api.app.domain.image.entity.QMemberImage.memberImage
import kr.co.fitview.api.app.domain.image.entity.enums.MemberImageType
import kr.co.fitview.api.app.domain.member.entity.QMember
import kr.co.fitview.api.app.domain.member.entity.QMember.member
import kr.co.fitview.api.app.domain.workout_history.entity.QWorkoutHistory.workoutHistory
import kr.co.fitview.api.app.domain.workout_partner.condition.AdminWorkoutPartnerRequestCondition
import kr.co.fitview.api.app.domain.workout_partner.condition.WorkoutPartnerRequestCondition
import kr.co.fitview.api.app.domain.workout_partner.dto.WorkoutPartnerRequestResponseForChatRoom
import kr.co.fitview.api.app.domain.workout_partner.dto.WorkoutPartnerRequestResponseForWorkoutPartner
import kr.co.fitview.api.app.domain.workout_partner.dto.request.enums.WorkoutPartnerRequestType
import kr.co.fitview.api.app.domain.workout_partner.dto.response.AdminWorkoutPartnerRequestResponse
import kr.co.fitview.api.app.domain.workout_partner.dto.response.WorkoutImageMinResponse
import kr.co.fitview.api.app.domain.workout_partner.dto.response.WorkoutPartnerRequestExpireResponse
import kr.co.fitview.api.app.domain.workout_partner.dto.response.WorkoutPartnerRequestResponse
import kr.co.fitview.api.app.domain.workout_partner.entity.QWorkoutPartnerRequest.workoutPartnerRequest
import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartnerRequest
import kr.co.fitview.api.app.domain.workout_partner.entity.enums.WorkoutPartnerRequestStatus
import kr.co.fitview.api.app.global.time.TimeHolder.time
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl

class WorkoutPartnerRequestRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
    private val em : EntityManager
) : WorkoutPartnerRequestRepositoryCustom {

    override fun findWorkoutPartnerByConditionAndDeletedAtIsNull(
        memberId: Long,
        condition: WorkoutPartnerRequestCondition
    ): Slice<WorkoutPartnerRequestResponse> {

        val workoutPartnerRequestResponses = findWorkoutPartnerByCondition(condition, memberId)

        val targetMemberIds = workoutPartnerRequestResponses.map { it.targetMemberId }

        val workoutPartnerWithWorkoutImageResponses = findWorkoutPartnerWithWorkoutImageUrlByMemberIds(targetMemberIds, workoutPartnerRequestResponses)

        val chatRoomResponses = findChatRoomByMemberIds(memberId, targetMemberIds)

        val responses: List<WorkoutPartnerRequestResponse> =
            workoutPartnerWithWorkoutImageResponses.map { it.toResponse(chatRoomResponses) }

        val hasNext = responses.size > condition.size!!

        val sliceContent = if (hasNext) responses.dropLast(1) else responses

        return SliceImpl(sliceContent, PageRequest.of(0, condition.size), hasNext)
    }


    private fun findWorkoutPartnerByCondition(
        condition: WorkoutPartnerRequestCondition,
        memberId: Long
    ): List<WorkoutPartnerRequestResponseForWorkoutPartner> {

        fun otherMemberJoinCondition(): BooleanExpression? {
            if (condition.type == WorkoutPartnerRequestType.RECEIVE) {
                return member.id.eq(workoutPartnerRequest.fromMember.id)
            }

            return member.id.eq(workoutPartnerRequest.toMember.id)
        }

        fun workoutPartnerRequestWhereCondition(): BooleanExpression? {
            if (condition.type == WorkoutPartnerRequestType.RECEIVE) {
                return workoutPartnerRequest.toMember.id.eq(memberId)
            }

            return workoutPartnerRequest.fromMember.id.eq(memberId)
        }

        fun lastRequestIdCondition(): BooleanExpression? {
            val lastId = condition.firstWorkoutPartnerRequestId ?: return null
            return workoutPartnerRequest.id.lt(lastId)
        }

        val limit = condition.size!! + 1

        val workoutPartnerRequestResponses = queryFactory
            .select(
                Projections.constructor(
                    WorkoutPartnerRequestResponseForWorkoutPartner::class.java,
                    workoutPartnerRequest.id,
                    workoutPartnerRequest.requestedAt,
                    workoutPartnerRequest.status,
                    image.url,
                    Expressions.nullExpression(String::class.java),
                    member.id,
                    member.nickname,
                    member.workoutExperience,
                    member.workoutStyle,
                    member.workoutGoal
                )
            )
            .from(workoutPartnerRequest)
            .join(member).on(otherMemberJoinCondition())
            .join(memberImage).on(memberImage.member.id.eq(member.id))
            .join(image).on(image.id.eq(memberImage.image.id))
            .where(
                lastRequestIdCondition(),
                workoutPartnerRequestWhereCondition(),
                memberImage.type.eq(MemberImageType.PROFILE),
                member.deletedAt.isNull,
                workoutPartnerRequest.deletedAt.isNull,
                memberImage.deletedAt.isNull,
                image.deletedAt.isNull,
            )
            .orderBy(workoutPartnerRequest.id.desc())
            .limit(limit.toLong())
            .fetch()

        return workoutPartnerRequestResponses
    }

    private fun findWorkoutPartnerWithWorkoutImageUrlByMemberIds(
        targetMemberIds: List<Long>,
        workoutPartnerRequestResponses: List<WorkoutPartnerRequestResponseForWorkoutPartner>
    ): List<WorkoutPartnerRequestResponseForWorkoutPartner> {
        val findWorkoutImages = queryFactory
            .select(
                Projections.constructor(
                    WorkoutImageMinResponse::class.java,
                    member.id,
                    image.url,
                    memberImage.seq
                )
            )
            .from(member)
            .join(member.mutableMemberImages, memberImage)
            .join(memberImage.image, image)
            .where(
                memberImage.type.eq(MemberImageType.WORKOUT),
                member.id.`in`(targetMemberIds)
            )
            .orderBy(member.id.asc(), memberImage.seq.asc())
            .fetch()

        val workoutImageMap = findWorkoutImages
            .groupBy { it.memberId }
            .mapValues { (_, images) -> images.minByOrNull { it.seq }!! }

        val workoutPartnerSecondResponses = workoutPartnerRequestResponses.map { request ->
            val imgUrl = workoutImageMap[request.targetMemberId]?.workoutImageUrl
            request.withWorkoutImageUrl(imgUrl)
        }
        return workoutPartnerSecondResponses
    }


    private fun findChatRoomByMemberIds(
        memberId: Long,
        targetMemberIds: List<Long>
    ): List<WorkoutPartnerRequestResponseForChatRoom> {

        val chatParticipant1 = QChatParticipant("chatParticipant1")
        val chatParticipant2 = QChatParticipant("chatParticipant2")

        val chatRoomResponses = queryFactory
            .select(
                Projections.constructor(
                    WorkoutPartnerRequestResponseForChatRoom::class.java,
                    chatParticipant2.member.id,
                    chatRoom.id
                )
            )
            .from(chatParticipant1)
            .join(chatParticipant2).on(chatParticipant1.chatRoom.id.eq(chatParticipant2.chatRoom.id))
            .join(chatRoom).on(chatRoom.id.eq(chatParticipant1.chatRoom.id))
            .where(
                chatRoom.type.eq(ChatRoomType.PRIVATE),
                chatParticipant1.member.id.eq(memberId),
                chatParticipant2.member.id.`in`(targetMemberIds)
            )
            .fetch()

        return chatRoomResponses
    }

    override fun findAllPendingWorkoutPartnerRequestAlreadyExpire(): List<WorkoutPartnerRequestExpireResponse> {

        return queryFactory
            .select(
                Projections.constructor(
                    WorkoutPartnerRequestExpireResponse::class.java,
                    workoutPartnerRequest.id
                )
            )
            .from(workoutPartnerRequest)
            .where(
                workoutPartnerRequest.status.eq(WorkoutPartnerRequestStatus.PENDING),
                workoutPartnerRequest.deletedAt.isNull,
                workoutPartnerRequest.requestedAt.loe(time.nowLocalDateTime.minusHours(24)),
//                workoutPartnerRequest.requestedAt.loe(time.nowLocalDateTime.minusMinutes(2)),
            )
            .fetch()
    }

    override fun updateExpireByIdIn(workoutPartnerRequestIds: List<Long>) {
        if (workoutPartnerRequestIds.isEmpty()) return

        queryFactory
            .update(workoutPartnerRequest)
            .set(workoutPartnerRequest.status, WorkoutPartnerRequestStatus.EXPIRE)
            .where(workoutPartnerRequest.id.`in`(workoutPartnerRequestIds))
            .execute()

        em.flush()
        em.clear()
    }

    override fun findAllWorkoutPartnerRequestBy(condition: AdminWorkoutPartnerRequestCondition): Slice<AdminWorkoutPartnerRequestResponse> {

        val fromMember = QMember("fromMember")
        val toMember = QMember("toMember")

        val pageSize = condition.size.toLong()

        val findWorkoutPartnerRequests = queryFactory
            .select(
                Projections.constructor(
                    AdminWorkoutPartnerRequestResponse::class.java,
                    workoutPartnerRequest.fromMember.id,
                    workoutPartnerRequest.toMember.id,
                    workoutPartnerRequest.id,
                    fromMember.nickname,
                    toMember.nickname,
                    workoutPartnerRequest.status,
                    workoutPartnerRequest.requestedAt,
                    workoutPartnerRequest.respondedAt,
                )
            )
            .from(workoutPartnerRequest)
            .join(fromMember)
                .on(fromMember.id.eq(workoutPartnerRequest.fromMember.id))
            .join(toMember)
                .on(toMember.id.eq(workoutPartnerRequest.toMember.id))
            .where(
                workoutPartnerRequest.deletedAt.isNull,
                condition.workoutPartnerRequestId?.let { workoutPartnerRequest.id.lt(it) }
            )
            .orderBy(workoutPartnerRequest.id.desc())
            .limit(pageSize + 1)
            .fetch()

        val hasNext = findWorkoutPartnerRequests.size > pageSize
        val content = if (hasNext) findWorkoutPartnerRequests.subList(0, pageSize.toInt()) else findWorkoutPartnerRequests

        val requestPairs = content.map { it.fromMemberId to it.toMemberId }

        val fromMemberChatParticipant = QChatParticipant("fromMemberChatParticipant")
        val toMemberChatParticipant = QChatParticipant("toMemberChatParticipant")

        val chatRoomPairs =
            queryFactory
                .select(
                    fromMemberChatParticipant.member.id,
                    toMemberChatParticipant.member.id
                )
                .from(fromMemberChatParticipant)
                .join(fromMemberChatParticipant.chatRoom, chatRoom)
                .join(toMemberChatParticipant)
                    .on(
                        toMemberChatParticipant.chatRoom.eq(chatRoom)
                    )
                .where(
                    fromMemberChatParticipant.member.id.`in`(requestPairs.map { it.first }),
                    toMemberChatParticipant.member.id.`in`(requestPairs.map { it.second }),
                    chatRoom.deletedAt.isNull
                )
                .fetch()
                .map { tuple ->
                    val fromId = tuple.get(fromMemberChatParticipant.member.id) as Long
                    val toId = tuple.get(toMemberChatParticipant.member.id) as Long
                    Pair(fromId, toId)
                }
                .toSet()

        val memberPairs = content.map {
            val small = minOf(it.fromMemberId, it.toMemberId)
            val large = maxOf(it.fromMemberId, it.toMemberId)
            small to large
        }

        val workoutHistoryCountMap: Map<Pair<Long, Long>, Long> =
            queryFactory
                .select(
                    workoutHistory.memberOne.id,
                    workoutHistory.memberTwo.id,
                    workoutHistory.count()
                )
                .from(workoutHistory)
                .where(
                    workoutHistory.memberOne.id.`in`(memberPairs.map { it.first }),
                    workoutHistory.memberTwo.id.`in`(memberPairs.map { it.second }),
                    workoutHistory.deletedAt.isNull
                )
                .groupBy(workoutHistory.memberOne.id, workoutHistory.memberTwo.id)
                .fetch()
                .associate { tuple ->
                    val m1 = tuple.get(workoutHistory.memberOne.id)!!
                    val m2 = tuple.get(workoutHistory.memberTwo.id)!!
                    val cnt = tuple.get(workoutHistory.count()) ?: 0L
                    (m1 to m2) to cnt
                }


        val result = content.map {
            val small = minOf(it.fromMemberId, it.toMemberId)
            val large = maxOf(it.fromMemberId, it.toMemberId)
            it.copy(
                hasChatRoom = chatRoomPairs.contains(it.fromMemberId to it.toMemberId),
                workoutHistoryCount = workoutHistoryCountMap[small to large] ?: 0L
            )
        }

        return SliceImpl(result, PageRequest.of(0, pageSize.toInt()), hasNext)
    }

    override fun findAllLatestWorkoutPartnerRequest(meMemberId: Long, otherMemberIds: List<Long>) : List<WorkoutPartnerRequest>{
        if (otherMemberIds.isEmpty()) {
            return emptyList()
        }

        val ascOtherMemberRequest = CaseBuilder()
            .`when`(workoutPartnerRequest.fromMember.id.eq(meMemberId)).then(1)
            .otherwise(0)
            .asc()

        val allRequests = queryFactory
            .selectFrom(workoutPartnerRequest)
            .where(
                (workoutPartnerRequest.fromMember.id.eq(meMemberId).and(workoutPartnerRequest.toMember.id.`in`(otherMemberIds)))
                .or(
                    (workoutPartnerRequest.toMember.id.eq(meMemberId).and(workoutPartnerRequest.fromMember.id.`in`(otherMemberIds)))
                )
            )
            .orderBy(
                workoutPartnerRequest.requestedAt.desc(),
                ascOtherMemberRequest
            )
            .fetch()

        return allRequests.distinctBy { request ->
            if (request.getFromMemberId() == meMemberId) request.getToMemberId() else request.getFromMemberId()
        }
    }

}