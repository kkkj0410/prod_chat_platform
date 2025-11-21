package kr.co.fitview.api.app.domain.chat.service

import kr.co.fitview.api.app.domain.chat.dto.response.LastChatMessage
import kr.co.fitview.api.app.domain.chat.repository.ChatMessageRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ChatMessageService(
    private val chatMessageRepository : ChatMessageRepository
) {

    fun findLastChatMessages(memberId: Long, chatRoomIds: List<Long>) : List<LastChatMessage>{
        return chatMessageRepository.findRecentChatMessageByMemberIdAndIn(memberId, chatRoomIds)
    }


}