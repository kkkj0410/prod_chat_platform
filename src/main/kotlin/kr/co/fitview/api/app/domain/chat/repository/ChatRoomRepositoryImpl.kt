package kr.co.fitview.api.app.domain.chat.repository

import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.fitview.api.app.domain.chat.condition.ChatRoomCondition
import kr.co.fitview.api.app.domain.chat.dto.response.ChatRoomResponse
import kr.co.fitview.api.app.domain.chat.dto.response.ChatRoomResponseFlat
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.QChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.QChatRoom.chatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.workout_partner.dto.WorkoutPartnerRequestResponseForWorkoutPartner
import kr.co.fitview.api.app.domain.workout_partner.dto.response.enums.WorkoutPartnerRequestStatusForResponse
import org.springframework.data.domain.Slice
import java.time.LocalDateTime

class ChatRoomRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : ChatRoomRepositoryCustom {

    override fun findPrivateChatRoomIdBetweenMemberIds(memberId1: Long, memberId2 : Long) : ChatRoom?{

        val chatParticipant1 = QChatParticipant("chatParticipant1")
        val chatParticipant2 = QChatParticipant("chatParticipant2")

        return queryFactory
            .selectFrom(chatRoom)
            .join(chatParticipant1).on(chatParticipant1.chatRoom.id.eq(chatRoom.id))
            .join(chatParticipant2).on(chatParticipant2.chatRoom.id.eq(chatRoom.id))
            .where(
                chatRoom.deletedAt.isNull,
                chatRoom.type.eq(ChatRoomType.PRIVATE),
                chatParticipant1.member.id.eq(memberId1),
                chatParticipant2.member.id.eq(memberId2)
            )
            .fetchOne()
    }

    override fun findChatRoomByDeletedAtIsNull(memberId: Long, condition: ChatRoomCondition): Slice<ChatRoomResponse> {
//        val chatRoomId: Long,
//        val profileImageUrl: String,
//        val nickname: String,
//        val isRead: Boolean,
//
//        val lastMessageId: Long,
//        val lastMessageType: ChatMessageType,
//        val lastMessageCreatedAt: LocalDateTime,
//        val lastMessageIsMe: Boolean,
//
//        val messageContent: String?,
//
//        val workoutRequestId: Long?,
//        val scheduledAt: LocalDateTime?,
//        val location: String?,
//
//        val lastWorkoutRequestStatus: WorkoutPartnerRequestStatusForResponse?
//        queryFactory
//            .select(
//                Projections.constructor(
//                    ChatRoomResponseFlat::class.java,
//
//                    )
//            )
//            .from(chatRoom)
//            .

        TODO()
    }

}