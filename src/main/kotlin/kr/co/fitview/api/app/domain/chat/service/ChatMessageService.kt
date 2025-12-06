package kr.co.fitview.api.app.domain.chat.service

import kr.co.fitview.api.app.domain.chat.condition.ChatMessageCondition
import kr.co.fitview.api.app.domain.chat.dto.ChatMessageAndWorkoutRequest
import kr.co.fitview.api.app.domain.chat.dto.request.*
import kr.co.fitview.api.app.domain.chat.dto.response.*
import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import kr.co.fitview.api.app.domain.chat.repository.ChatMessageRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.domain.workout.dto.response.enums.WorkoutRequestStatusForResponse
import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import kr.co.fitview.api.app.domain.workout.service.WorkoutRequestService
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.chat.ChatErrorCode
import kr.co.fitview.api.app.global.time.Time
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class ChatMessageService(
    private val chatMessageRepository: ChatMessageRepository,
    private val chatParticipantQueryService: ChatParticipantQueryService,
    private val workoutRequestService: WorkoutRequestService,
    private val messageReadStatusService : MessageReadStatusService,
    private val memberQueryService : MemberQueryService,
    private val time: Time
) {

    @Transactional
    fun addChatTextMessage(member: Member, chatRoom: ChatRoom, message: ChatTextMessageServiceRequest) : StompChatTextMessage {
        val now = time.nowLocalDateTime
        val chatMessage = ChatMessage.ofText(
            member = member,
            chatRoom = chatRoom,
            content = message.content,
            sentAt = now
        )
        chatRoom.updateLastMessageAt(now)

        chatMessageRepository.save(chatMessage)

        messageReadStatusService.saveMessageReadStatus(
            member = member,
            chatMessage = chatMessage,
            chatRoom = chatRoom
        )

        val response = StompChatTextMessage(
            chatMessageId = chatMessage.id!!,
            sentAt = now,
            content = message.content
        )

        return response
    }

    @Transactional
    fun addChatWorkoutRequestMessage(fromMember: Member, chatRoom: ChatRoom, message: ChatWorkoutRequestMessageServiceRequest) : StompChatWorkoutRequestMessage {
        val now = time.nowLocalDateTime

        validateScheduledAtNotPast(message.scheduledAt, now)

        val chatMessage = ChatMessage.ofWorkoutRequest(
            member = fromMember,
            chatRoom = chatRoom,
            sentAt = now
        )
        chatRoom.updateLastMessageAt(now)
        chatMessageRepository.save(chatMessage)

        val findOtherChatParticipant = chatParticipantQueryService.findOtherParticipantFromMemberIdAndChatRoomId(fromMember.id!!, chatRoom.id!!)

        val toMember = memberQueryService.findMemberReferenceFrom(findOtherChatParticipant!!.getMemberId())

        val savedWorkoutRequest = workoutRequestService.addWorkoutRequest(
            chatRoomId = chatRoom.id!!,
            chatMessage = chatMessage,
            fromMember = fromMember,
            toMember = toMember,
            message = message
        )

        messageReadStatusService.saveMessageReadStatus(
            member = fromMember,
            chatMessage = chatMessage,
            chatRoom = chatRoom
        )

        val response = StompChatWorkoutRequestMessage(
            chatMessageId = chatMessage.id!!,
            sentAt = now,
            workoutRequestId = savedWorkoutRequest.id!!,
            status = WorkoutRequestStatusForResponse.from(
                dbStatus = WorkoutRequestStatus.PENDING,
                requestedAt = now,
                scheduledAt = message.scheduledAt,
                now = now
            ),
            scheduledAt = message.scheduledAt,
            location = message.location,
        )

        return response
    }

    private fun validateScheduledAtNotPast(
        scheduledAt: LocalDateTime,
        now: LocalDateTime
    ) {
        if (scheduledAt.isBefore(now)) {
            throw GlobalException(ChatErrorCode.WORKOUT_REQUEST_TIME_PAST)
        }
    }


}