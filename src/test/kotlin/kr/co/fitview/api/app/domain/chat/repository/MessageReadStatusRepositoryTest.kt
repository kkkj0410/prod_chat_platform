package kr.co.fitview.api.app.domain.chat.repository

import jakarta.persistence.EntityManager
import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.MessageReadStatus
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class MessageReadStatusRepositoryTest @Autowired  constructor(
    private val messageReadStatusRepository : MessageReadStatusRepository,
    val chatRoomRepository: ChatRoomRepository,
    val chatParticipantRepository : ChatParticipantRepository,
    val chatMessageRepository : ChatMessageRepository,
    val oAuth2Service : OAuth2Service,
    val memberRepository : MemberRepository,
    val time : Time,
    val em : EntityManager
) : IntegrationTestSupport(){

    @DisplayName("해당 채팅방에서 회원은 모든 메시지를 읽음 처리한다.")
    @Test
    fun updateAllMessageReadStatusBy() {
        // given
        val me = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        val other = Member(
            email = "email2",
            password = "password2",
            role = Role.USER,
        )
        val savedMember1 = memberRepository.save(me)
        val savedMember2 = memberRepository.save(other)

        val savedChatRoom = chatRoomRepository.save(ChatRoom(ChatRoomType.PRIVATE))

        val chatParticipant1 = ChatParticipant(
            savedChatRoom,
            savedMember1
        )
        val chatParticipant2 = ChatParticipant(
            savedChatRoom,
            savedMember2
        )
        chatParticipantRepository.save(chatParticipant1)
        chatParticipantRepository.save(chatParticipant2)

        val message1 = ChatMessage(
            member = me,
            chatRoom = savedChatRoom,
            type = ChatMessageType.TEXT,
            content = "content",
            sentAt = time.nowLocalDateTime.minusHours(3)
        )
        val message2 = ChatMessage(
            member = other,
            chatRoom = savedChatRoom,
            type = ChatMessageType.TEXT,
            content = "content",
            sentAt = time.nowLocalDateTime
        )
        val message3 = ChatMessage(
            member = other,
            chatRoom = savedChatRoom,
            type = ChatMessageType.TEXT,
            content = "content",
            sentAt = time.nowLocalDateTime
        )
        val message4 = ChatMessage(
            member = other,
            chatRoom = savedChatRoom,
            type = ChatMessageType.TEXT,
            content = "content",
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(message1)
        chatMessageRepository.save(message2)
        chatMessageRepository.save(message3)
        chatMessageRepository.save(message4)

        val messageRead1 = MessageReadStatus(
            chatRoom = savedChatRoom,
            member = me,
            chatMessage = message1,
            isRead = true
        )
        val messageRead2 = MessageReadStatus(
            chatRoom = savedChatRoom,
            member = me,
            chatMessage = message2,
            isRead = false
        )
        val messageRead3 = MessageReadStatus(
            chatRoom = savedChatRoom,
            member = me,
            chatMessage = message3,
            isRead = false
        )
        val messageRead4 = MessageReadStatus(
            chatRoom = savedChatRoom,
            member = me,
            chatMessage = message4,
            isRead = false
        )
        messageReadStatusRepository.save(messageRead1)
        messageReadStatusRepository.save(messageRead2)
        messageReadStatusRepository.save(messageRead3)
        messageReadStatusRepository.save(messageRead4)

        // when
        messageReadStatusRepository.updateAllMessageReadStatusBy(me.id!!, savedChatRoom.id!!)

        em.flush()
        em.clear()

        // then
        val findMessages = messageReadStatusRepository.findAll()
        val findMember = memberRepository.findById(me.id!!).orElseThrow()
        assertThat(findMessages).hasSize(4)
        assertThat(findMessages)
            .extracting("member", "isRead")
            .containsExactlyInAnyOrder(
                tuple(findMember, true),
                tuple(findMember, true),
                tuple(findMember, true),
                tuple(findMember, true),
            )
    }
}