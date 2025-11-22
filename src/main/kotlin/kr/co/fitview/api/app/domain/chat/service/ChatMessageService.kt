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
    private val time: Time
) {

    @Transactional
    fun addChatTextMessage(member: Member, chatRoom: ChatRoom, message: ChatTextMessageServiceRequest) {
        val chatMessage = ChatMessage(
            member = member,
            chatRoom = chatRoom,
            type = ChatMessageType.TEXT,
            content = message.content
        )
        chatRoom.updateLastMessageAt(time.nowLocalDateTime)
        chatMessageRepository.save(chatMessage)
    }

    @Transactional
    fun addChatWorkoutRequestMessage(fromMember: Member, chatRoom: ChatRoom, message: ChatWorkoutRequestMessageServiceRequest) {
        val chatMessage = ChatMessage(
            member = fromMember,
            chatRoom = chatRoom,
            type = ChatMessageType.TEXT,
        )
        chatRoom.updateLastMessageAt(time.nowLocalDateTime)
        chatMessageRepository.save(chatMessage)

        val findOtherChatParticipant = chatParticipantService.findOtherParticipantFromMemberIdAndChatRoomId(fromMember.id!!, chatRoom.id!!)

        val toMember = memberReferenceProvider.findMemberReferenceFrom(findOtherChatParticipant!!.getMemberId())

        workoutRequestService.addWorkoutRequest(
            chatMessage = chatMessage,
            fromMember = fromMember,
            toMember = toMember,
            message = message
        )
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