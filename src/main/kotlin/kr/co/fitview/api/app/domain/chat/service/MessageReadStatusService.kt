package kr.co.fitview.api.app.domain.chat.service

import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.MessageReadStatus
import kr.co.fitview.api.app.domain.chat.entity.QMessageReadStatus.messageReadStatus
import kr.co.fitview.api.app.domain.chat.repository.MessageReadStatusRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.service.MemberReferenceProvider
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class MessageReadStatusService(
    private val messageReadStatusRepository : MessageReadStatusRepository,
    private val chatParticipantService : ChatParticipantService,
    private val memberReferenceProvider: MemberReferenceProvider
) {

    @Transactional
    fun saveMessageReadStatus(member : Member, chatMessage : ChatMessage, chatRoom : ChatRoom) : List<MessageReadStatus>{

        val findOtherChatParticipant = chatParticipantService.findOtherParticipantFromMemberIdAndChatRoomId(member.id!!, chatRoom.id!!)

        val otherMember = memberReferenceProvider.findMemberReferenceFrom(findOtherChatParticipant!!.getMemberId())

        val messageReadStatuses = createMessageReadStatuses(member, otherMember, chatRoom, chatMessage)

        return messageReadStatusRepository.saveAll(messageReadStatuses)
    }

    @Transactional
    fun modifyMessageReadStatusFrom(memberId: Long, chatRoomId: Long) {
        messageReadStatusRepository.updateAllMessageReadStatusBy(memberId, chatRoomId)
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