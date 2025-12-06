package kr.co.fitview.api.app.domain.chat.service

import kr.co.fitview.api.app.domain.chat.entity.ChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.repository.ChatParticipantRepository
import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class ChatParticipantService(
    private val chatParticipantRepository : ChatParticipantRepository,
    private val memberQueryService: MemberQueryService
) {
    @Transactional
    fun saveChatParticipants(chatRoom : ChatRoom, fromMemberId : Long, toMemberId : Long){
        chatParticipantRepository.saveAll(createChatParticipants(chatRoom, fromMemberId, toMemberId))
    }

    private fun createChatParticipants(
        chatRoom: ChatRoom,
        fromMemberId: Long,
        toMemberId : Long
    ) : List<ChatParticipant>{
        return listOf(fromMemberId, toMemberId).map { memberId ->
            createChatParticipant(chatRoom, memberId)
        }
    }

    private fun createChatParticipant(
        chatRoom: ChatRoom,
        fromMemberId: Long
    ) = ChatParticipant(
        chatRoom = chatRoom,
        member = memberQueryService.findMemberReferenceFrom(fromMemberId)
    )
}