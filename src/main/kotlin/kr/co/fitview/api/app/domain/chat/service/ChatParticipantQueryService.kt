package kr.co.fitview.api.app.domain.chat.service

import kr.co.fitview.api.app.domain.chat.entity.ChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.repository.ChatParticipantRepository
import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class ChatParticipantQueryService(
    private val chatParticipantRepository : ChatParticipantRepository,
) {

    fun findChatRoomFromMemberIdAndChatRoomId(memberId : Long, chatRoomId : Long) : ChatParticipant? {
        return chatParticipantRepository.findChatParticipantByMemberIdAndChatRoomId(memberId, chatRoomId)
    }

    fun findOtherParticipantFromMemberIdAndChatRoomId(memberId : Long, chatRoomId : Long) : ChatParticipant? {
        return chatParticipantRepository.findOtherChatParticipantByMemberIdAndChatRoomId(memberId, chatRoomId)
    }
}