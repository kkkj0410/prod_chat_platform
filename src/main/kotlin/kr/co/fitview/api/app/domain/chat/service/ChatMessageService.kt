package kr.co.fitview.api.app.domain.chat.service

import kr.co.fitview.api.app.domain.chat.dto.request.*
import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.QChatRoom.chatRoom
import kr.co.fitview.api.app.domain.chat.repository.ChatMessageRepository
import kr.co.fitview.api.app.domain.fcm.dto.request.EventFcmChatMessage
import kr.co.fitview.api.app.domain.member.dto.response.ChatRoomMemberProfile
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.domain.workout_history.service.WorkoutHistoryQueryService
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.global.GlobalErrorCode
import kr.co.fitview.api.app.global.exception.error.member.MemberErrorCode
import kr.co.fitview.api.app.global.stomp.dto.request.StompEventTextMessageDepth1
import kr.co.fitview.api.app.global.stomp.dto.request.StompEventTextMessageDepth2
import kr.co.fitview.api.app.global.stomp.dto.request.StompEventTextMessageDepth3
import kr.co.fitview.api.app.global.time.Time
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class ChatMessageService(
    private val chatMessageRepository: ChatMessageRepository,
    private val messageReadStatusService : MessageReadStatusService,
    private val workoutHistoryQueryService : WorkoutHistoryQueryService,
    private val memberQueryService : MemberQueryService,
    private val publisher: ApplicationEventPublisher,
    private val time: Time
) {

    @Transactional
    fun saveChatTextMessage(member: Member, chatRoom: ChatRoom, message: ChatTextMessageServiceRequest, clientRequestId : String? = null) : ChatMessage {
        val now = time.nowLocalDateTime

        chatRoom.updateLastMessageAt(now)

        val savedChatMessage = saveChatMessage(member, chatRoom, message, now)

        messageReadStatusService.saveMessageReadStatus(
            member = member,
            chatMessage = savedChatMessage,
            chatRoom = chatRoom
        )

        sendStompChatTextMessage(
            fromMember = member,
            chatRoom = chatRoom,
            chatMessage = savedChatMessage,
            clientRequestId = clientRequestId
        )

        sendFcmChatTextMessage(member, chatRoom.id!!, savedChatMessage.id!!)

        return savedChatMessage
    }

    @Transactional
    fun addWorkoutRequestMessage(fromMember: Member, chatRoom: ChatRoom, now: LocalDateTime) : ChatMessage {
        val chatMessage = ChatMessage.ofWorkoutRequest(
            member = fromMember,
            chatRoom = chatRoom,
            sentAt = now
        )
        return chatMessageRepository.save(chatMessage)
    }


    private fun saveChatMessage(
        member: Member,
        chatRoom: ChatRoom,
        message: ChatTextMessageServiceRequest,
        now: LocalDateTime
    ): ChatMessage {
        val chatMessage = ChatMessage.ofText(
            member = member,
            chatRoom = chatRoom,
            content = message.content,
            sentAt = now
        )
        chatMessageRepository.save(chatMessage)
        return chatMessage
    }


    private fun sendStompChatTextMessage(
        fromMember: Member,
        chatRoom: ChatRoom,
        chatMessage: ChatMessage,
        clientRequestId : String?
    ) {
        val toMember = memberQueryService.findOtherMemberFrom(fromMember.id!!, chatRoom.id!!)
            ?: throw GlobalException(GlobalErrorCode.ENTITY_NOT_FOUND)

        val isCompleteWorkout = workoutHistoryQueryService.existsWorkoutHistoryFrom(chatRoom.id!!)
        val meProfile = memberQueryService.findMemberProfileFrom(fromMember.id!!, chatRoom.id!!)
            ?: throw GlobalException(GlobalErrorCode.ENTITY_NOT_FOUND)

        val meStompMessage = createStompTextMessage(
            memberId = fromMember.id!!,
            chatRoom = chatRoom,
            isCompleteWorkout = isCompleteWorkout,
            meProfile = meProfile,
            chatMessage = chatMessage,
            isMe = true,
            clientRequestId = clientRequestId
        )
        val otherStompMessage = createStompTextMessage(
            memberId = toMember.id!!,
            chatRoom = chatRoom,
            isCompleteWorkout = isCompleteWorkout,
            meProfile = meProfile,
            chatMessage = chatMessage,
            isMe = false,
            clientRequestId = clientRequestId

        )

        publisher.publishEvent(meStompMessage)
        publisher.publishEvent(otherStompMessage)
    }

    private fun createStompTextMessage(
        memberId: Long,
        chatRoom: ChatRoom,
        isCompleteWorkout: Boolean,
        meProfile: ChatRoomMemberProfile,
        chatMessage: ChatMessage,
        isMe : Boolean,
        clientRequestId : String?
    ) : StompEventTextMessageDepth1 {
        return StompEventTextMessageDepth1(
            memberId = memberId,
            clientRequestId = clientRequestId,
            message = StompEventTextMessageDepth2(
                chatRoomId = chatRoom.id!!,
                isCompleteWorkout = isCompleteWorkout,
                profileImageUrl = meProfile.profileImageUrl,
                nickname = meProfile.nickname,
                chatMessage = StompEventTextMessageDepth3(
                    chatMessageId = chatMessage.id!!,
                    content = chatMessage.content!!,
                    sentAt = chatMessage.sentAt!!,
                    isMe = isMe
                )
            )
        )
    }

    private fun sendFcmChatTextMessage(
        member: Member,
        chatRoomId: Long,
        chatMessageId: Long
    ) {
        val toMember = memberQueryService.findOtherMemberFrom(member.id!!, chatRoomId)
            ?: throw GlobalException(MemberErrorCode.MEMBER_NOT_FOUND)

        val fcmChatMessage = EventFcmChatMessage(
            toMemberId = toMember.id!!,
            fromNickname = member.nickname!!,
            chatRoomId = chatRoomId,
            chatMessageId = chatMessageId
        )
        publisher.publishEvent(fcmChatMessage)
    }

}