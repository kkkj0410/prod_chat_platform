package kr.co.fitview.api.app.domain.chat.service

import kr.co.fitview.api.app.domain.chat.condition.ChatCondition
import kr.co.fitview.api.app.domain.chat.dto.response.ChatMessageContent
import kr.co.fitview.api.app.domain.chat.dto.response.ChatMessageWorkoutRequest
import kr.co.fitview.api.app.domain.chat.dto.response.LastChatMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import kr.co.fitview.api.app.domain.chat.repository.ChatMessageRepository
import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequest
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
    private val chatMessageRepository : ChatMessageRepository,
    private val chatParticipantService: ChatParticipantService,
    private val time : Time
) {

    fun findLastChatMessages(memberId: Long, chatRoomIds: List<Long>) : List<LastChatMessage>{
        return chatMessageRepository.findRecentChatMessageByMemberIdAndIn(memberId, chatRoomIds)
    }

    fun findChatMessages(memberId : Long, chatRoomId : Long, condition : ChatCondition) : Slice<LastChatMessage> {
        val findChatParticipant = chatParticipantService.findChatRoomFromMemberIdAndChatRoomId(memberId, chatRoomId)

        if(findChatParticipant != null){
            throw GlobalException(ChatErrorCode.NOT_MEMBER_OF_CHAT_ROOM)
        }

        val slice = chatMessageRepository.findChatMessageByCondition(chatRoomId, condition)

        val content = slice.content

        val responses = content.map { chatMessageAndWorkoutRequest ->
            when (chatMessageAndWorkoutRequest.chatMessage.type) {
                ChatMessageType.TEXT -> ChatMessageContent.from(chatMessageAndWorkoutRequest.chatMessage, memberId)
                ChatMessageType.WORKOUT_REQUEST -> ChatMessageWorkoutRequest.from(
                    chatMessage = chatMessageAndWorkoutRequest.chatMessage,
                    workoutRequest = chatMessageAndWorkoutRequest.workoutRequest!!,
                    myMemberId = memberId,
                    now = time.nowLocalDateTime
                )
                else -> throw IllegalArgumentException("Unknown ChatMessageType: ${chatMessageAndWorkoutRequest.chatMessage.type}")
            }
        }


        return SliceImpl(responses, slice.pageable, slice.hasNext())
    }

//    private fun isMemberInChatRoom(memberId: Long, chatRoomId: Long) {
//        chatParticipantService
//            .findChatRoomFromMemberIdAndChatRoomId(memberId, chatRoomId)
//            ?.let { throw GlobalException(ChatErrorCode.NOT_MEMBER_OF_CHAT_ROOM) }
//    }


}