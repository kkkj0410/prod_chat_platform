package kr.co.fitview.api.app.domain.chat.service

import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.repository.ChatRoomRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ChatRoomQueryService(
    private val chatRoomRepository : ChatRoomRepository
) {

    fun findChatRoomFrom(workoutRequestId : Long) : ChatRoom?{
        return chatRoomRepository.findChatRoomByWorkoutRequestId(workoutRequestId)
    }

    fun findAllChatRoomReferenceFrom(chatRoomIds: List<Long>): List<ChatRoom> {
        return chatRoomIds.map { id ->
            chatRoomRepository.getReferenceById(id)
        }
    }

    fun findChatRoomFrom(fromMemberId: Long, toMemberId: Long): ChatRoom? {
        return chatRoomRepository.findPrivateChatRoomIdBetweenMemberIds(fromMemberId, toMemberId)
    }

    fun findChatRoomReferenceFrom(chatRoomId: Long): ChatRoom {
        return chatRoomRepository.getReferenceById(chatRoomId)
    }

}