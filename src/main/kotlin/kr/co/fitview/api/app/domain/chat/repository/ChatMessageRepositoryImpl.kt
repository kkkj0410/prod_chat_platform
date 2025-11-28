package kr.co.fitview.api.app.domain.chat.repository

import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import kr.co.fitview.api.app.domain.chat.condition.ChatCondition
import kr.co.fitview.api.app.domain.chat.dto.ChatMessageAndWorkoutRequest
import kr.co.fitview.api.app.domain.chat.dto.response.ChatMessageContent
import kr.co.fitview.api.app.domain.chat.dto.response.ChatMessageWorkoutRequest
import kr.co.fitview.api.app.domain.chat.dto.response.LastChatMessage
import kr.co.fitview.api.app.domain.chat.entity.QChatMessage.chatMessage
import kr.co.fitview.api.app.domain.chat.entity.QChatRoom.chatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import kr.co.fitview.api.app.domain.workout.dto.response.enums.WorkoutRequestStatusForResponse
import kr.co.fitview.api.app.domain.workout.entity.QWorkoutRequest.workoutRequest
import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import kr.co.fitview.api.app.global.enums.Direction
import kr.co.fitview.api.app.global.time.Time
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import java.sql.Timestamp

class ChatMessageRepositoryImpl(
    private val queryFactory : JPAQueryFactory,
    private val em : EntityManager,
    private val time : Time
) : ChatMessageRepositoryCustom {

    override fun findRecentChatMessageByMemberIdAndIn(memberId: Long, chatRoomIds: List<Long>) : List<LastChatMessage>{

        val inClause = chatRoomIds.joinToString(",")
        val sql = """
        WITH RankedMessages AS (
            SELECT
                cm_inner.chat_message_id,
                cm_inner.chat_room_id,
                cm_inner.type,
                cm_inner.content,
                cm_inner.sent_at,
                cm_inner.deleted_at,
                cm_inner.member_id,
                ROW_NUMBER() OVER (
                    PARTITION BY cm_inner.chat_room_id
                    ORDER BY cm_inner.sent_at DESC
                ) AS rn
            FROM chat_message cm_inner
            WHERE cm_inner.chat_room_id IN ($inClause)
        )
        SELECT
                    rm.chat_message_id,      -- 0
                    rm.chat_room_id,         -- 1
                    rm.type,                 -- 2
                    rm.content,              -- 3
                    rm.sent_at,              -- 4
                    rm.member_id,			 -- 5
                    mrs.is_read,             -- 6
                    wr.workout_request_id,   -- 7
                    wr.status,               -- 8
                    wr.requested_at,         -- 9
                    wr.scheduled_at,         -- 10
                    wr.location              -- 11
        FROM
            RankedMessages rm
        LEFT JOIN
            message_read_status mrs
        ON
            mrs.chat_message_id = rm.chat_message_id
        AND
            mrs.member_id = :memberId
        LEFT JOIN
            workout_request wr
        ON
            wr.chat_message_id = rm.chat_message_id
        WHERE 
            rm.rn = 1
        AND
            rm.deleted_at is null
        AND
            mrs.deleted_at is null
        AND
            wr.deleted_at is null;
    """.trimIndent()

        val query = em
            .createNativeQuery(sql)
            .setParameter("memberId", memberId)

        @Suppress("UNCHECKED_CAST")
        val result = query.resultList as List<Array<Any?>>

        return result.map { row ->

            val chatMessageId = row[0] as Long
            val chatRoomId = row[1] as Long
            val type = ChatMessageType.valueOf(row[2] as String)
            val content = row[3] as String?
            val sentAt = (row[4] as Timestamp).toLocalDateTime()
            val targetMemberId = row[5] as Long
            val isMe = isMe(targetMemberId, memberId)
            val isRead = if (isMe) true else (row[6] as? Boolean) ?: false

            val workoutRequestId = row[7] as Long?
            val status = (row[8] as? String)?.let { WorkoutRequestStatus.valueOf(it) }
            val requestedAt = (row[9] as? Timestamp)?.toLocalDateTime()
            val scheduledAt = (row[10] as? Timestamp)?.toLocalDateTime()
            val location = row[11] as String?

            if (workoutRequestId != null) {
                ChatMessageWorkoutRequest(
                    chatMessageId = chatMessageId,
                    type = type,
                    sentAt = sentAt,
                    isMe = isMe,
                    isRead = isRead,
                    chatRoomId = chatRoomId,
                    workoutRequestId = workoutRequestId,
                    status = WorkoutRequestStatusForResponse.from(
                        dbStatus = status!!,
                        requestedAt = requestedAt!!,
                        scheduledAt = scheduledAt!!,
                        now = time.nowLocalDateTime
                    ),
                    scheduledAt = scheduledAt,
                    location = location!!,
                    memberId = targetMemberId
                )
            } else {
                ChatMessageContent(
                    chatMessageId = chatMessageId,
                    type = type,
                    sentAt = sentAt,
                    isMe = isMe,
                    isRead = isRead,
                    chatRoomId = chatRoomId,
                    content = content ?: "",
                    memberId = targetMemberId
                )
            }
        }
    }

    override fun findChatMessageByCondition(chatRoomId: Long, condition: ChatCondition) : Slice<ChatMessageAndWorkoutRequest> {

//        fun ltLastMessageAt(): BooleanExpression? {
//            return condition.lastMessageAt()?.let { lastAt ->
//                chatMessage.sentAt.lt(lastAt)
//            }
//        }
//
//        val result = queryFactory
//            .select(chatMessage, workoutRequest)
//            .from(chatMessage)
//            .join(chatRoom)
//            .on(
//                chatRoom.id.eq(chatMessage.chatRoom.id),
//                chatRoom.id.eq(chatRoomId)
//            )
//            .leftJoin(workoutRequest).fetchJoin()
//            .on(workoutRequest.chatMessage.id.eq(chatMessage.id))
//            .where(ltLastMessageAt())
//            .orderBy(chatMessage.sentAt.desc().nullsLast())
//            .limit((condition.size!! + 1).toLong())
//            .fetch()
//
//        val hasNext = result.size > condition.size
//        val content = if (hasNext) result.subList(0, condition.size) else result
//
//        val mappedEntity = content.map { tuple ->
//            ChatMessageAndWorkoutRequest(
//                chatMessage = tuple.get(chatMessage)!!,
//                workoutRequest = tuple.get(workoutRequest)
//            )
//        }
//
//        return SliceImpl(mappedEntity, PageRequest.of(0, condition.size), hasNext)

        fun ltLastMessageAt(): BooleanExpression? {
            return condition.lastMessageAt()?.let { lastAt ->
                if (condition.direction == Direction.ASC) {
                    chatMessage.sentAt.gt(lastAt)
                } else {
                    chatMessage.sentAt.lt(lastAt)
                }
            }
        }

        val orderSpecifier = if (condition.direction == Direction.ASC) {
            chatMessage.sentAt.asc().nullsLast()
        } else {
            chatMessage.sentAt.desc().nullsLast()
        }

        val result = queryFactory
            .select(chatMessage, workoutRequest)
            .from(chatMessage)
            .join(chatRoom)
            .on(
                chatRoom.id.eq(chatMessage.chatRoom.id),
                chatRoom.id.eq(chatRoomId)
            )
            .leftJoin(workoutRequest).fetchJoin()
            .on(workoutRequest.chatMessage.id.eq(chatMessage.id))
            .where(ltLastMessageAt())
            .orderBy(orderSpecifier)
            .limit((condition.size!! + 1).toLong())
            .fetch()

        val hasNext = result.size > condition.size
        val content = if (hasNext) result.subList(0, condition.size) else result

        val mappedEntity = content.map { tuple ->
            ChatMessageAndWorkoutRequest(
                chatMessage = tuple.get(chatMessage)!!,
                workoutRequest = tuple.get(workoutRequest)
            )
        }

        return SliceImpl(mappedEntity, PageRequest.of(0, condition.size), hasNext)
    }

    private fun isMe(targetMemberId: Long, memberId: Long) = targetMemberId == memberId

}