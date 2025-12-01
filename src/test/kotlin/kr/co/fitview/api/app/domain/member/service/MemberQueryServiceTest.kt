package kr.co.fitview.api.app.domain.member.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.entity.ChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.chat.repository.ChatParticipantRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatRoomRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.util.TestDataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class MemberQueryServiceTest @Autowired constructor(
    val memberQueryService: MemberQueryService,
    val memberRepository : MemberRepository,
    val oAuth2Service : OAuth2Service,
    val chatRoomRepository : ChatRoomRepository,
    val chatParticipantRepository : ChatParticipantRepository
) : IntegrationTestSupport(){

    @DisplayName("운동 파트너 요청에서 요청자의 프로필을 조회한다.")
    @Test
    fun findMemberWorkoutRequestProfileFrom() {
        // given
        val me = Member(
            email = "email1",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(me)

        val request = TestDataFactory.oAuth2SignupRequest()
        oAuth2Service.signup(request, me.id!!)

        // when
        val findMemberProfile = memberQueryService.findMemberWorkoutRequestProfileFrom(me.id!!)

        // then
        assertThat(findMemberProfile)
            .extracting("memberId", "profileImageUrl", "nickname")
            .contains(me.id!!, request.profileImageUrl, request.nickname)
    }

    @DisplayName("개인 채팅방의 상대 회원 프로필을 조회한다.")
    @Test
    fun findOtherMemberChatRoomProfile() {
        // given
        val me = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(me)
        val signupRequest1 = TestDataFactory.oAuth2SignupRequest()
        oAuth2Service.signup(signupRequest1, me.id!!)

        val other = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(other)
        val signupRequest2 = TestDataFactory.oAuth2SignupRequest(
            nickname = "updateNick",
            profileImageUrl = "updateProfile"
        )
        oAuth2Service.signup(signupRequest2, other.id!!)

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
        val response = memberQueryService.findOtherMemberChatRoomProfile(me.id!!, chatRoom.id!!)

        // then
        assertThat(response)
            .extracting("memberId", "nickname", "profileImageUrl")
            .contains(other.id!!, signupRequest2.nickname, signupRequest2.profileImageUrl)
    }
}