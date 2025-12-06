package kr.co.fitview.api.app.domain.chat.service

import kr.co.fitview.api.app.domain.chat.dto.MemberPair
import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.MessageReadStatus
import kr.co.fitview.api.app.domain.chat.repository.MessageReadStatusRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class MessageReadStatusService(
    private val messageReadStatusRepository : MessageReadStatusRepository,
    private val chatParticipantService : ChatParticipantService,
    private val memberQueryService : MemberQueryService
) {

    @Transactional
    fun saveMessageReadStatus(member : Member, chatMessage : ChatMessage, chatRoom : ChatRoom) : List<MessageReadStatus>{

        val findOtherChatParticipant = chatParticipantService.findOtherParticipantFromMemberIdAndChatRoomId(member.id!!, chatRoom.id!!)

        val otherMember = memberQueryService.findMemberReferenceFrom(findOtherChatParticipant!!.getMemberId())

        val messageReadStatuses = createMessageReadStatuses(member, otherMember, chatRoom, chatMessage)

        return messageReadStatusRepository.saveAll(messageReadStatuses)
    }

    @Transactional
    fun modifyMessageReadStatusFrom(memberId: Long, chatRoomId: Long) {
        messageReadStatusRepository.updateAllMessageReadStatusBy(memberId, chatRoomId)
    }

    @Transactional
    fun addAllMessageReadStatusFrom(
        chatRooms: List<ChatRoom>,
        chatMessages: List<ChatMessage>,
        memberPairs: List<MemberPair>
    ) {
        val statuses = mutableListOf<MessageReadStatus>()

        chatMessages.forEachIndexed { idx, chatMessage ->
            val chatRoom = chatRooms[idx]
            val req = memberPairs[idx]

            val memberOne = memberQueryService.findMemberReferenceFrom(req.memberOneId)
            val memberTwo = memberQueryService.findMemberReferenceFrom(req.memberTwoId)

            statuses += MessageReadStatus(
                chatRoom = chatRoom,
                chatMessage = chatMessage,
                member = memberOne,
                isRead = false
            )
            statuses += MessageReadStatus(
                chatRoom = chatRoom,
                chatMessage = chatMessage,
                member = memberTwo,
                isRead = false
            )
        }

        messageReadStatusRepository.saveAll(statuses)
    }

    private fun createMessageReadStatuses(
        meMember: Member,
        otherMember: Member,
        chatRoom: ChatRoom,
        chatMessage: ChatMessage,
    ) : List<MessageReadStatus> {

        val meMessageReadStatus = MessageReadStatus(
            chatRoom = chatRoom,
            member = meMember,
            chatMessage = chatMessage,
            isRead = true
        )
        val otherMessageReadStatus = MessageReadStatus(
            chatRoom = chatRoom,
            member = otherMember,
            chatMessage = chatMessage,
            isRead = false
        )

        return listOf(meMessageReadStatus, otherMessageReadStatus)
    }

}