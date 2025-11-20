package kr.co.fitview.api.app.domain.chat.repository

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.auth.entity.RefreshTokenStatus
import kr.co.fitview.api.app.domain.chat.entity.ChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.QChatRoom.chatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.global.entity.Role
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ChatRoomRepositoryTest @Autowired constructor(
    val chatRoomRepository: ChatRoomRepository,
    val chatParticipantRepository : ChatParticipantRepository,
    val memberRepository : MemberRepository
) : IntegrationTestSupport() {

    @DisplayName("두 회원이 참석한 개인 채팅방을 조회한다.")
    @Test
    fun findPrivateChatRoomIdBetweenMemberIds() {
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
        val findChatRoom = chatRoomRepository.findPrivateChatRoomIdBetweenMemberIds(
            member1.id!!, member2.id!!
        )

        // then
        assertThat(findChatRoom!!.id).isEqualTo(savedChatRoom.id)
    }
}