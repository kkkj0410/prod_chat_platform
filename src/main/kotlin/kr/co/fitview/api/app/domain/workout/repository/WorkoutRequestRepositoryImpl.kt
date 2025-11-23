package kr.co.fitview.api.app.domain.workout.repository

import com.fasterxml.jackson.annotation.JsonIgnore
import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import kr.co.fitview.api.app.domain.chat.entity.QChatMessage.chatMessage
import kr.co.fitview.api.app.domain.chat.entity.QChatRoom.chatRoom
import kr.co.fitview.api.app.domain.workout.dto.response.LastWorkoutRequestMessage
import kr.co.fitview.api.app.domain.workout.dto.response.WorkoutRequestUpdateResponse
import kr.co.fitview.api.app.domain.workout.dto.response.enums.WorkoutRequestStatusForResponse
import kr.co.fitview.api.app.domain.workout.entity.QWorkoutRequest.workoutRequest
import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import kr.co.fitview.api.app.global.time.Time
import java.sql.Timestamp

class WorkoutRequestRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
    private val em: EntityManager,
    private val time : Time
) : WorkoutRequestRepositoryCustom {

    override fun findRecentWorkoutRequest(chatRoomIds: List<Long>): List<LastWorkoutRequestMessage> {

        val inClause = chatRoomIds.joinToString(",")
        val sql = """
        WITH RankedRequests AS (
            SELECT
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
        r.status,
        r.requested_at,
        r.scheduled_at,
        r.chat_room_id
        FROM RankedRequests r
        WHERE rn = 1;
    """.trimIndent()

        val query = em
            .createNativeQuery(sql)

        @Suppress("UNCHECKED_CAST")
        val result = query.resultList as List<Array<Any?>>


        return result.map { r ->
            val dbStatusStr = r[0] as String?
            val dbStatus = dbStatusStr?.let { WorkoutRequestStatus.valueOf(it) }
            val requestedAt = (r[1] as Timestamp).toLocalDateTime()
            val scheduledAt = (r[2] as Timestamp).toLocalDateTime()
            val chatRoomId = r[3] as Long

            LastWorkoutRequestMessage(
                status = WorkoutRequestStatusForResponse.from(dbStatus!!, requestedAt, scheduledAt, time.nowLocalDateTime),
                chatRoomId = chatRoomId
            )
        }

    }

    override fun findAllExpireWorkoutRequest() : List<WorkoutRequestUpdateResponse> {


        val expireByScheduledAt =queryFactory
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

}