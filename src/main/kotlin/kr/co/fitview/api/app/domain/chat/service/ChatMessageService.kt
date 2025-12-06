package kr.co.fitview.api.app.domain.chat.service

import kr.co.fitview.api.app.domain.chat.dto.request.*
import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.repository.ChatMessageRepository
import kr.co.fitview.api.app.domain.member.dto.response.ChatRoomMemberProfile
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.domain.notification.dto.response.*
import kr.co.fitview.api.app.domain.workout.entity.QWorkoutRequest.workoutRequest
import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequest
import kr.co.fitview.api.app.domain.workout_history.service.WorkoutHistoryQueryService
import kr.co.fitview.api.app.domain.workout_history.service.WorkoutHistoryService
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.global.GlobalErrorCode
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
    fun saveChatTextMessage(member: Member, chatRoom: ChatRoom, message: ChatTextMessageServiceRequest) : ChatMessage {
        val now = time.nowLocalDateTime

        chatRoom.updateLastMessageAt(now)

        val savedChatMessage = saveChatMessage(member, chatRoom, message, now)

        messageReadStatusService.saveMessageReadStatus(
            member = member,
            chatMessage = savedChatMessage,
            chatRoom = chatRoom
        )

        sendStompWorkoutRequestMessage(member, chatRoom, savedChatMessage)

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


    private fun sendStompWorkoutRequestMessage(
        fromMember: Member,
        chatRoom: ChatRoom,
        chatMessage: ChatMessage,
    ) {
        val toMember = memberQueryService.findOtherMemberFrom(fromMember.id!!, chatRoom.id!!)
            ?: throw GlobalException(GlobalErrorCode.ENTITY_NOT_FOUND)

        val isCompleteWorkout = workoutHistoryQueryService.existsWorkoutHistoryFrom(chatRoom.id!!)
        val meProfile = memberQueryService.findMemberProfileFrom(fromMember.id!!, chatRoom.id!!)
            ?: throw GlobalException(GlobalErrorCode.ENTITY_NOT_FOUND)

        val meStompMessage = createStompTextMessage(fromMember.id!!, chatRoom, isCompleteWorkout, meProfile, chatMessage, true)
        val otherStompMessage = createStompTextMessage(toMember.id!!, chatRoom, isCompleteWorkout, meProfile, chatMessage, false)

        publisher.publishEvent(meStompMessage)
        publisher.publishEvent(otherStompMessage)
    }

    private fun createStompTextMessage(
        memberId: Long,
        chatRoom: ChatRoom,
        isCompleteWorkout: Boolean,
        meProfile: ChatRoomMemberProfile,
        chatMessage: ChatMessage,
        isMe : Boolean
    ) : StompEventTextMessageDepth1{
        return StompEventTextMessageDepth1(
            memberId = memberId,
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

}