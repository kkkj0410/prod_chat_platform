package kr.co.fitview.api.app.domain.chat.service

import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.chat.repository.ChatRoomRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class ChatRoomService(
    private val chatRoomRepository : ChatRoomRepository
) {

    fun findChatRoomFrom(fromMemberId: Long, toMemberId: Long): ChatRoom? {
        return chatRoomRepository.findPrivateChatRoomIdBetweenMemberIds(fromMemberId, toMemberId)
    }

    fun addPrivateChatRoom() : ChatRoom {
        val chatRoom = ChatRoom(ChatRoomType.PRIVATE)
        return chatRoomRepository.save(chatRoom)
    }

    fun findPrivateChatRoomFrom(memberOneId : Long, memberTwoId : Long) : ChatRoom? {
        return chatRoomRepository.findPrivateChatRoomIdBetweenMemberIds(memberOneId, memberTwoId)
    }
}