package kr.co.fitview.api.app.domain.chat.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.entity.ChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.QChatParticipant.chatParticipant
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.chat.repository.ChatParticipantRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatRoomRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ChatParticipantServiceTest@Autowired constructor(
    val chatParticipantService: ChatParticipantService,
    val chatRoomRepository : ChatRoomRepository,
    val chatParticipantRepository : ChatParticipantRepository,
    val memberRepository : MemberRepository,
    val time : Time
) : IntegrationTestSupport() {

    @DisplayName("해당 회원이 타겟 채팅방을 사용하고 있는지 확인한다.")
    @Test
    fun findChatRoomFromMemberIdAndChatRoomId() {
        //given
        val member = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member)


        val chatRoom = chatRoomRepository.save(ChatRoom(ChatRoomType.PRIVATE))

        val chatParticipant = ChatParticipant(
            chatRoom,
            member
        )
        chatParticipantRepository.save(chatParticipant)

        // when
        val findChatParticipant = chatParticipantService.findChatRoomFromMemberIdAndChatRoomId(member.id!!, chatRoom.id!!)

        // then
        assertThat(findChatParticipant!!.id).isEqualTo(chatParticipant.id!!)
        assertThat(findChatParticipant.member).isEqualTo(member)
    }


    @DisplayName("채팅방에 참여한 본인 외 다른 한명을 찾는다.")
    @Test
    fun findOtherParticipantFromMemberIdAndChatRoomId() {
        // given
        val me = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        val other = Member(
            email = "email2",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(me)
        memberRepository.save(other)

        val chatRoom = chatRoomRepository.save(ChatRoom(ChatRoomType.PRIVATE))

        val chatParticipant1 = ChatParticipant(
            chatRoom,
            me
        )
        val chatParticipant2 = ChatParticipant(
            chatRoom,
            other
        )
        chatParticipantRepository.save(chatParticipant1)
        chatParticipantRepository.save(chatParticipant2)

        // when
        val findOtherChatParticipant = chatParticipantService.findOtherParticipantFromMemberIdAndChatRoomId(me.id!!, chatRoom.id!!)

        // then
        assertThat(findOtherChatParticipant)
            .extracting("chatRoom", "member")
            .contains(chatRoom, other)
    }

}