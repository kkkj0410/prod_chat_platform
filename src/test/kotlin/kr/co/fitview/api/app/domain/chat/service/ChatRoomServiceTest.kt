package kr.co.fitview.api.app.domain.chat.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.entity.ChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.chat.repository.ChatParticipantRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatRoomRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.global.entity.Role
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ChatRoomServiceTest @Autowired constructor(
    private val chatRoomService : ChatRoomService,
    private val chatRoomRepository : ChatRoomRepository,
    private val chatParticipantRepository : ChatParticipantRepository,
    private val memberRepository : MemberRepository
) : IntegrationTestSupport() {


    @DisplayName("두 회원간의 개인 채팅방을 조회한다.")
    @Test
    fun findChatRoomFrom() {
        // given
        val member1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        val member2 = Member(
            email = "email2",
            password = "password2",
            role = Role.USER,
        )
        val savedMember1 = memberRepository.save(member1)
        val savedMember2 = memberRepository.save(member2)

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

        // when
        val findChatRoom = chatRoomService.findChatRoomFrom(member1.id!!, member2.id!!)

        // then
        assertThat(findChatRoom!!.id).isNotNull()
        assertThat(findChatRoom.type).isEqualTo(ChatRoomType.PRIVATE)
    }

    @DisplayName("두 회원간의 채팅방이 없으면 채팅방 조회 하지 못한다.")
    @Test
    fun findChatRoomFromNoneChatRoom() {
        // given
        val member1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        val member2 = Member(
            email = "email2",
            password = "password2",
            role = Role.USER,
        )
        memberRepository.save(member1)
        memberRepository.save(member2)

        // when
        val findChatRoom = chatRoomService.findChatRoomFrom(member1.id!!, member2.id!!)

        // then
        assertThat(findChatRoom).isNull()
    }

    @DisplayName("비밀 채팅방을 생성한다.")
    @Test
    fun addPrivateChatRoom() {
        // when
        val savedChatRoom = chatRoomService.addPrivateChatRoom()

        // then
        assertThat(savedChatRoom.id).isNotNull()
        assertThat(savedChatRoom.type).isEqualTo(ChatRoomType.PRIVATE)
    }

    @DisplayName("두 회원간의 개인 채팅방을 조회한다.")
    @Test
    fun findPrivateChatRoomFrom() {
        //given
        val member1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        val member2 = Member(
            email = "email2",
            password = "password2",
            role = Role.USER,
        )
        val savedMember1 = memberRepository.save(member1)
        val savedMember2 = memberRepository.save(member2)

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

        // when
        val findChatRoom = chatRoomService.findPrivateChatRoomFrom(
            member1.id!!, member2.id!!
        )

        // then
        assertThat(findChatRoom!!.id).isEqualTo(savedChatRoom.id)
    }
}