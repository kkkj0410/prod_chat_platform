package kr.co.fitview.api.app.domain.chat.repository

import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import kr.co.fitview.api.app.domain.chat.condition.ChatMessageCondition
import kr.co.fitview.api.app.domain.chat.dto.ChatMessageAndWorkoutRequest
import kr.co.fitview.api.app.domain.chat.dto.response.ChatMessageContent
import kr.co.fitview.api.app.domain.chat.dto.response.ChatMessageWorkoutRequest
import kr.co.fitview.api.app.domain.chat.dto.response.ChatNoticeMessageResponse
import kr.co.fitview.api.app.domain.chat.dto.response.LastChatMessage
import kr.co.fitview.api.app.domain.chat.entity.QChatMessage.chatMessage
import kr.co.fitview.api.app.domain.chat.entity.QChatNoticeMessage.chatNoticeMessage
import kr.co.fitview.api.app.domain.chat.entity.QChatRoom.chatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatNoticeMessageType
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import kr.co.fitview.api.app.domain.workout.dto.response.enums.WorkoutRequestStatusForResponse
import kr.co.fitview.api.app.domain.workout.entity.QWorkoutRequest.workoutRequest
import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import kr.co.fitview.api.app.global.enums.Direction
import kr.co.fitview.api.app.global.slice.SliceWithBefore
import kr.co.fitview.api.app.global.time.Time
import org.springframework.data.domain.PageRequest
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
                    wr.location,             -- 11
                    cnm.type,                -- 12
                    cnm.workout_history_id   -- 13
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
        LEFT JOIN
            chat_notice_message cnm
        ON
            cnm.chat_message_id = rm.chat_message_id
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
            val targetMemberId = row[5] as Long?
            val isRead = (row[6] as Boolean?) ?: false

            val workoutRequestId = row[7] as Long?
            val status = (row[8] as? String)?.let { WorkoutRequestStatus.valueOf(it) }
            val requestedAt = (row[9] as? Timestamp)?.toLocalDateTime()
            val scheduledAt = (row[10] as? Timestamp)?.toLocalDateTime()
            val location = row[11] as String?

            val chatMessageNoticeType = row[12] as String?
            val workoutHistoryId = row[13] as Long?


            if (type == ChatMessageType.WORKOUT_REQUEST) {
                val isMe = isMe(targetMemberId!!, memberId)
                ChatMessageWorkoutRequest(
                    chatMessageId = chatMessageId,
                    type = type,
                    sentAt = sentAt,
                    isMe = isMe,
                    isRead = if (isMe) true else isRead,
                    chatRoomId = chatRoomId,
                    workoutRequestId = workoutRequestId!!,
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
            } else if(type == ChatMessageType.TEXT) {
                val isMe = isMe(targetMemberId!!, memberId)
                ChatMessageContent(
                    chatMessageId = chatMessageId,
                    type = type,
                    sentAt = sentAt,
                    isMe = isMe,
                    isRead = if (isMe) true else isRead,
                    chatRoomId = chatRoomId,
                    content = content ?: "",
                    memberId = targetMemberId
                )
            }else{
                ChatNoticeMessageResponse(
                    chatMessageId = chatMessageId,
                    type = type,
                    sentAt = sentAt,
                    isRead = isRead,
                    chatRoomId = chatRoomId,
                    content = ChatNoticeMessageType.valueOf(chatMessageNoticeType!!),
                    workoutHistoryId = workoutHistoryId
                )
            }
        }
    }



    override fun findChatMessageByCondition(chatRoomId: Long, condition: ChatMessageCondition) : SliceWithBefore<ChatMessageAndWorkoutRequest> {

        fun messagePredicate(): BooleanExpression? {
            val lastAt = condition.lastMessageAt()
            val targetId = condition.targetChatMessageId

            val predicates = mutableListOf<BooleanExpression>()

            targetId?.let {
                val targetPredicate = if (condition.direction == Direction.ASC) {
                    chatMessage.id.goe(it)
                } else {
                    chatMessage.id.loe(it)
                }
                predicates.add(targetPredicate)
            }

            lastAt?.let {
                val timePredicate = if (condition.direction == Direction.ASC) {
                    chatMessage.sentAt.gt(it)
                } else {
                    chatMessage.sentAt.lt(it)
                }
                predicates.add(timePredicate)
            }

            return predicates.reduceOrNull { acc, expr -> acc.and(expr) }
        }

        val orderSpecifier = if (condition.direction == Direction.ASC) {
            listOf(chatMessage.sentAt.asc().nullsLast(), chatMessage.id.asc())
        } else {
            listOf(chatMessage.sentAt.desc().nullsLast(), chatMessage.id.desc())
        }

        val result = queryFactory
            .select(chatMessage, workoutRequest, chatNoticeMessage)
            .from(chatMessage)
            .join(chatRoom)
            .on(
                chatRoom.id.eq(chatMessage.chatRoom.id),
                chatRoom.id.eq(chatRoomId)
            )
            .leftJoin(workoutRequest)
            .on(workoutRequest.chatMessage.id.eq(chatMessage.id))
            .leftJoin(chatNoticeMessage)
            .on(chatNoticeMessage.chatMessage.id.eq(chatMessage.id))
            .where(messagePredicate())
            .orderBy(*orderSpecifier.toTypedArray())
            .limit((condition.size!! + 1).toLong())
            .fetch()



        fun reverseMessagePredicate(): BooleanExpression? {
            val lastAt = condition.lastMessageAt()
            val targetId = condition.targetChatMessageId

            val predicates = mutableListOf<BooleanExpression>()

            targetId?.let {
                val targetPredicate = if (condition.direction == Direction.ASC) {
                    chatMessage.id.lt(it)
                } else {
                    chatMessage.id.gt(it)
                }
                predicates.add(targetPredicate)
            }

            lastAt?.let {
                val timePredicate = if (condition.direction == Direction.ASC) {
                    chatMessage.sentAt.loe(it)
                } else {
                    chatMessage.sentAt.goe(it)
                }
                predicates.add(timePredicate)
            }

            return predicates.reduceOrNull { acc, expr -> acc.and(expr) }
        }


        val reverseOrderSpecifier = if (condition.direction == Direction.ASC) {
            listOf(chatMessage.sentAt.desc().nullsLast(), chatMessage.id.desc())
        } else {
            listOf(chatMessage.sentAt.asc().nullsLast(), chatMessage.id.asc())
        }

        val hasBefore = queryFactory
            .selectOne()
            .from(chatMessage)
            .join(chatRoom)
            .on(
                chatRoom.id.eq(chatMessage.chatRoom.id),
                chatRoom.id.eq(chatRoomId)
            )
            .leftJoin(workoutRequest)
            .on(workoutRequest.chatMessage.id.eq(chatMessage.id))
            .leftJoin(chatNoticeMessage)
            .on(chatNoticeMessage.chatMessage.id.eq(chatMessage.id))
            .where(reverseMessagePredicate())
            .orderBy(*reverseOrderSpecifier.toTypedArray())
            .limit(1)
            .fetchFirst() != null

        val hasNext = result.size > condition.size
        val content = if (hasNext) result.subList(0, condition.size) else result

        val mappedEntity = content.map { tuple ->
            ChatMessageAndWorkoutRequest(
                chatMessage = tuple.get(chatMessage)!!,
                workoutRequest = tuple.get(workoutRequest),
                chatNoticeMessage = tuple.get(chatNoticeMessage)
            )
        }

        return SliceWithBefore(mappedEntity, PageRequest.of(0, condition.size), hasNext, hasBefore)
    }

    private fun isMe(targetMemberId: Long, memberId: Long) = targetMemberId == memberId

}