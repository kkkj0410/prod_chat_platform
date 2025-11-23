package kr.co.fitview.api.app.domain.chat.service

import kr.co.fitview.api.app.domain.chat.condition.ChatCondition
import kr.co.fitview.api.app.domain.chat.dto.ChatMessageAndWorkoutRequest
import kr.co.fitview.api.app.domain.chat.dto.request.*
import kr.co.fitview.api.app.domain.chat.dto.response.ChatMessageContent
import kr.co.fitview.api.app.domain.chat.dto.response.ChatMessageWorkoutRequest
import kr.co.fitview.api.app.domain.chat.dto.response.LastChatMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import kr.co.fitview.api.app.domain.chat.repository.ChatMessageRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.service.MemberReferenceProvider
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

@Service
@Transactional(readOnly = true)
class ChatMessageService(
    private val chatMessageRepository: ChatMessageRepository,
    private val chatParticipantService: ChatParticipantService,
    private val workoutRequestService: WorkoutRequestService,
    private val memberReferenceProvider : MemberReferenceProvider,
    private val messageReadStatusService : MessageReadStatusService,
    private val time: Time
) {

    @Transactional
    fun addChatTextMessage(member: Member, chatRoom: ChatRoom, message: ChatTextMessageServiceRequest) : LastChatMessage {
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

        val response = ChatMessageContent(
            chatMessageId = chatMessage.id!!,
            sentAt = now,
            content = message.content
        )

        return response
    }

    @Transactional
    fun addChatWorkoutRequestMessage(fromMember: Member, chatRoom: ChatRoom, message: ChatWorkoutRequestMessageServiceRequest) : LastChatMessage{
        val now = time.nowLocalDateTime
        val chatMessage = ChatMessage.ofWorkoutRequest(
            member = fromMember,
            chatRoom = chatRoom,
            sentAt = now
        )
        chatRoom.updateLastMessageAt(now)
        chatMessageRepository.save(chatMessage)

        val findOtherChatParticipant = chatParticipantService.findOtherParticipantFromMemberIdAndChatRoomId(fromMember.id!!, chatRoom.id!!)

        val toMember = memberReferenceProvider.findMemberReferenceFrom(findOtherChatParticipant!!.getMemberId())

        val savedWorkoutRequest = workoutRequestService.addWorkoutRequest(
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

        val response = ChatMessageWorkoutRequest(
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


    fun findLastChatMessages(memberId: Long, chatRoomIds: List<Long>): List<LastChatMessage> {
        return chatMessageRepository.findRecentChatMessageByMemberIdAndIn(memberId, chatRoomIds)
    }

    fun findChatMessages(memberId: Long, chatRoomId: Long, condition: ChatCondition): Slice<LastChatMessage> {
        validateMemberInChatRoom(memberId, chatRoomId)

        val slice = chatMessageRepository.findChatMessageByCondition(chatRoomId, condition)
        val content = slice.content

        val responses =
            content.map { chatMessageAndWorkoutRequest -> mapChatMessage(chatMessageAndWorkoutRequest, memberId) }

        return SliceImpl(responses, slice.pageable, slice.hasNext())
    }

    private fun validateMemberInChatRoom(memberId: Long, chatRoomId: Long) {
        chatParticipantService.findChatRoomFromMemberIdAndChatRoomId(memberId, chatRoomId)
            ?: throw GlobalException(ChatErrorCode.NOT_MEMBER_OF_CHAT_ROOM)
    }

    private fun mapChatMessage(chatAndRequest: ChatMessageAndWorkoutRequest, memberId: Long): LastChatMessage {
        return when (chatAndRequest.chatMessage.type) {
            ChatMessageType.TEXT ->
                ChatMessageContent.from(chatAndRequest.chatMessage, memberId)

            ChatMessageType.WORKOUT_REQUEST ->
                ChatMessageWorkoutRequest.from(
                    chatMessage = chatAndRequest.chatMessage,
                    workoutRequest = chatAndRequest.workoutRequest!!,
                    myMemberId = memberId,
                    now = time.nowLocalDateTime
                )

            else -> throw IllegalArgumentException("Unknown ChatMessageType: ${chatAndRequest.chatMessage.type}")
        }
    }


}