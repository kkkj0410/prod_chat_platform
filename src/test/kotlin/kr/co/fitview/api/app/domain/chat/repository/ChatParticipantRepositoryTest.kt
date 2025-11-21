package kr.co.fitview.api.app.domain.chat.repository

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.address.dto.request.AddressCreateServiceRequest
import kr.co.fitview.api.app.domain.address.entity.enums.AddressSiDo
import kr.co.fitview.api.app.domain.chat.condition.ChatCondition
import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle
import kr.co.fitview.api.app.domain.member.entity.enums.WorkoutTimeName
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.oauth2.dto.request.OAuth2SignupServiceRequest
import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.global.entity.Gender
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate
import java.time.ZoneId

class ChatParticipantRepositoryTest @Autowired constructor(
    val chatRoomRepository: ChatRoomRepository,
    val chatParticipantRepository : ChatParticipantRepository,
    val chatMessageRepository : ChatMessageRepository,
    val oAuth2Service : OAuth2Service,
    val memberRepository : MemberRepository,
    val time : Time
) : IntegrationTestSupport() {


    @DisplayName("회원이 해당 채팅방을 사용하고있는지 확인한다.")
    @Test
    fun findChatParticipantByMemberIdAndChatRoomId() {
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
        val findChatParticipant = chatParticipantRepository.findChatParticipantByMemberIdAndChatRoomId(member.id!!, chatRoom.id!!)

        // then
        assertThat(findChatParticipant!!.id).isEqualTo(chatParticipant.id!!)
        assertThat(findChatParticipant.member).isEqualTo(member)

    }

}