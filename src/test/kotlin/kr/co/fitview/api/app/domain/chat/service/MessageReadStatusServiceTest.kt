package kr.co.fitview.api.app.domain.chat.service

import jakarta.persistence.EntityManager
import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.dto.MemberPair
import kr.co.fitview.api.app.domain.chat.entity.*
import kr.co.fitview.api.app.domain.chat.entity.QChatMessage.chatMessage
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.chat.repository.ChatMessageRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatParticipantRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatRoomRepository
import kr.co.fitview.api.app.domain.chat.repository.MessageReadStatusRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class MessageReadStatusServiceTest @Autowired constructor(
    private val messageReadStatusService : MessageReadStatusService,
    private val messageReadStatusRepository: MessageReadStatusRepository,
    private val chatRoomRepository : ChatRoomRepository,
    private val chatParticipantRepository : ChatParticipantRepository,
    private val chatMessageRepository : ChatMessageRepository,
    private val memberRepository : MemberRepository,
    private val time : Time,
    private val em : EntityManager
) : IntegrationTestSupport(){


    @DisplayName("회원이 메시지를 보낼 시, 채팅방에 있는 모든 회원에 대한 메시지 읽음 여부를 저장한다.")
    @Test
    fun saveMessageReadStatus() {
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
        memberRepository.save(me)
        memberRepository.save(other)

        val savedChatRoom = chatRoomRepository.save(ChatRoom(ChatRoomType.PRIVATE))

        val chatParticipant1 = ChatParticipant(
            savedChatRoom,
            me
        )
        val chatParticipant2 = ChatParticipant(
            savedChatRoom,
            other
        )
        chatParticipantRepository.save(chatParticipant1)
        chatParticipantRepository.save(chatParticipant2)

        val chatMessage = ChatMessage(
            member = me,
            chatRoom = savedChatRoom,
            type = ChatMessageType.TEXT,
            content = "content",
            sentAt = time.nowLocalDateTime.minusHours(20)
        )
        chatMessageRepository.save(chatMessage)

        // when
        messageReadStatusService.saveMessageReadStatus(
            member = me,
            chatMessage = chatMessage,
            chatRoom = savedChatRoom,
        )

        // then
        val findMessageReadStatuses = messageReadStatusRepository.findAll()

        assertThat(findMessageReadStatuses)
            .extracting("chatRoom", "member", "chatMessage", "isRead")
            .containsExactlyInAnyOrder(
                tuple(savedChatRoom, me, chatMessage, true),
                tuple(savedChatRoom, other, chatMessage, false),
            )
    }

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
        messageReadStatusService.modifyMessageReadStatusFrom(me.id!!, savedChatRoom.id!!)

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

    @DisplayName("안내 메시지를 채팅방에 개시할 시, 읽음 여부를 모두 저장한다.")
    @Test
    fun addAllMessageReadStatusFrom() {
        // given
        val member1 = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(member1)
        val member2 = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(member2)
        val member3 = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(member3)

        val chatRoom1 = ChatRoom(type = ChatRoomType.PRIVATE)
        val chatRoom2 = ChatRoom(type = ChatRoomType.PRIVATE)
        val chatRooms = listOf(chatRoom1, chatRoom2)
        chatRoomRepository.saveAll(chatRooms)

        val chatMessage1 = ChatMessage.ofText(
            member = member1,
            chatRoom = chatRoom1,
            content = "content1",
            sentAt = time.nowLocalDateTime
        )
        val chatMessage2 = ChatMessage.ofText(
            member = member1,
            chatRoom = chatRoom2,
            content = "content1",
            sentAt = time.nowLocalDateTime
        )
        val chatMessages = listOf(chatMessage1, chatMessage2)
        chatMessageRepository.saveAll(chatMessages)

        val memberPairs = listOf(
            MemberPair(memberOneId = member1.id!!, memberTwoId = member2.id!!),
            MemberPair(memberOneId = member1.id!!, memberTwoId = member3.id!!),
        )


        // when
        messageReadStatusService.addAllMessageReadStatusFrom(
            chatRooms = chatRooms,
            chatMessages = chatMessages,
            memberPairs = memberPairs
        )

        // then
        val findMessageReadStatuses = messageReadStatusRepository.findAll()
        assertThat(findMessageReadStatuses).hasSize(4)

        assertThat(findMessageReadStatuses)
            .extracting("chatRoom", "member", "chatMessage", "isRead")
            .containsExactlyInAnyOrder(
                tuple(chatRoom1, member1, chatMessage1, false),
                tuple(chatRoom1, member2, chatMessage1, false),
                tuple(chatRoom2, member1, chatMessage2, false),
                tuple(chatRoom2, member3, chatMessage2, false),
            )
    }
}