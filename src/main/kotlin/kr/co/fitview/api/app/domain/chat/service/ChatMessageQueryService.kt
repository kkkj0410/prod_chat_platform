package kr.co.fitview.api.app.domain.chat.service

import kr.co.fitview.api.app.domain.chat.condition.ChatMessageCondition
import kr.co.fitview.api.app.domain.chat.dto.ChatMessageAndWorkoutRequest
import kr.co.fitview.api.app.domain.chat.dto.response.ChatMessageContent
import kr.co.fitview.api.app.domain.chat.dto.response.ChatMessageWorkoutRequest
import kr.co.fitview.api.app.domain.chat.dto.response.ChatNoticeMessageResponse
import kr.co.fitview.api.app.domain.chat.dto.response.LastChatMessage
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import kr.co.fitview.api.app.domain.chat.repository.ChatMessageRepository
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.chat.ChatErrorCode
import kr.co.fitview.api.app.global.slice.SliceWithBefore
import kr.co.fitview.api.app.global.time.Time
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ChatMessageQueryService(
    private val chatMessageRepository: ChatMessageRepository,
    private val chatParticipantQueryService: ChatParticipantQueryService,
    private val time: Time
) {

    fun findLastChatMessages(memberId: Long, chatRoomIds: List<Long>): List<LastChatMessage> {
        return chatMessageRepository.findRecentChatMessageByMemberIdAndIn(memberId, chatRoomIds)
    }

    fun findChatMessages(memberId: Long, chatRoomId: Long, condition: ChatMessageCondition): SliceWithBefore<LastChatMessage> {
        validateMemberInChatRoom(memberId, chatRoomId)

        val slice = chatMessageRepository.findChatMessageByCondition(chatRoomId, condition)
        val content = slice.content

        val responses =
            content.map { chatMessageAndWorkoutRequest -> mapChatMessage(chatMessageAndWorkoutRequest, memberId) }

        val sortedResponses = responses.sortedWith(
            compareByDescending<LastChatMessage> { it.sentAt }
                .thenByDescending { it.chatMessageId }
        )

        return SliceWithBefore(sortedResponses, slice.pageable, slice.hasNext(), slice.hasBefore)
    }

    private fun validateMemberInChatRoom(memberId: Long, chatRoomId: Long) {
        chatParticipantQueryService.findChatParticipantFromMemberIdAndChatRoomId(memberId, chatRoomId)
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

            ChatMessageType.NOTICE ->
                ChatNoticeMessageResponse.from(chatAndRequest.chatMessage, chatAndRequest.chatNoticeMessage!!)


            else -> throw IllegalArgumentException("Unknown ChatMessageType: ${chatAndRequest.chatMessage.type}")
        }
    }
}