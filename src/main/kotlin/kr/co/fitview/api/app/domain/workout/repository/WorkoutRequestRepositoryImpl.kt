package kr.co.fitview.api.app.domain.workout.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import kr.co.fitview.api.app.domain.workout.dto.response.LastWorkoutRequestMessage
import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import java.sql.Timestamp
import java.time.LocalDateTime

class WorkoutRequestRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
    private val em: EntityManager
) : WorkoutRequestRepositoryCustom {

    override fun findRecentWorkoutRequest(chatRoomIds: List<Long>): List<LastWorkoutRequestMessage> {

        val inClause = chatRoomIds.joinToString(",")
        val sql = """
        WITH RankedRequests AS (
            SELECT
                wr.status,
                wr.requested_at,
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
        r.chat_room_id
        FROM RankedRequests r
        WHERE rn = 1;
    """.trimIndent()

        val query = em
            .createNativeQuery(sql)

        @Suppress("UNCHECKED_CAST")
        val result = query.resultList as List<Array<Any?>>


        return result.map { row ->
            LastWorkoutRequestMessage(
                dbStatus = (row[0] as String?)?.let { WorkoutRequestStatus.valueOf(it) },
                requestedAt =  (row[1] as Timestamp).toLocalDateTime(),
                chatRoomId = row[2] as Long
            )
        }

    }

}