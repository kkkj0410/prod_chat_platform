package kr.co.fitview.api.app.domain.chat.service

import kr.co.fitview.api.app.domain.chat.dto.request.*
import kr.co.fitview.api.app.domain.chat.dto.response.*
import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.repository.ChatMessageRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.domain.workout.dto.response.enums.WorkoutRequestStatusForResponse
import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import kr.co.fitview.api.app.domain.workout.service.WorkoutRequestService
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.chat.ChatErrorCode
import kr.co.fitview.api.app.global.time.Time
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class ChatMessageService(
    private val chatMessageRepository: ChatMessageRepository,
    private val messageReadStatusService : MessageReadStatusService,
    private val time: Time
) {

    @Transactional
    fun addChatTextMessage(member: Member, chatRoom: ChatRoom, message: ChatTextMessageServiceRequest) : ChatMessage {
        val now = time.nowLocalDateTime

        chatRoom.updateLastMessageAt(now)

        val savedChatMessage = saveChatMessage(member, chatRoom, message, now)

        messageReadStatusService.saveMessageReadStatus(
            member = member,
            chatMessage = savedChatMessage,
            chatRoom = chatRoom
        )

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


}