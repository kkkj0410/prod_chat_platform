package kr.co.fitview.api.app.domain.member.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.entity.ChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.QChatRoom.chatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.chat.repository.ChatParticipantRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatRoomRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.member.MemberErrorCode
import kr.co.fitview.api.app.global.exception.error.workout_request.WorkoutRequestErrorCode
import kr.co.fitview.api.app.global.util.TestDataFactory
import org.assertj.core.api.Assertions.*
import org.assertj.core.api.ThrowingConsumer
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


    @DisplayName("채팅방의 각 회원을 조회한다.")
    @Test
    fun findChatMemberFromOrElseThrow() {
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


        val other2 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(other2)
        val signupRequest3 = TestDataFactory.oAuth2SignupRequest(
            nickname = "updateNick",
            profileImageUrl = "updateProfile"
        )
        oAuth2Service.signup(signupRequest3, other2.id!!)

        val chatRoom2 = chatRoomRepository.save(ChatRoom(ChatRoomType.PRIVATE))

        val chatParticipant3 = ChatParticipant(
            chatRoom2,
            me
        )
        val chatParticipant4 = ChatParticipant(
            chatRoom2,
            other2
        )
        chatParticipantRepository.save(chatParticipant3)
        chatParticipantRepository.save(chatParticipant4)

        // when
        val response = memberQueryService.findChatMemberFromOrElseThrow(me.id!!, chatRoom.id!!)

        // then
        assertThat(response.me)
            .extracting("memberId", "nickname", "profileImageUrl")
            .contains(me.id!!, signupRequest1.nickname, signupRequest1.profileImageUrl)

        assertThat(response.other)
            .extracting("memberId", "nickname", "profileImageUrl")
            .contains(other.id!!, signupRequest2.nickname, signupRequest2.profileImageUrl)
    }

    @DisplayName("채팅방의 각 회원 조회가 안되면 에러를 발생시킨다.")
    @Test
    fun findChatMemberFromOrElseThrowNotFoundMember() {
        // when & then
        assertThatThrownBy {
            memberQueryService.findChatMemberFromOrElseThrow(123L, 123L)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND)
            })
    }

    @DisplayName("회원의 채팅방 프로필을 확인한다.")
    @Test
    fun findMemberChatProfileFrom() {
        // given
        val me = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(me)

        val request = TestDataFactory.oAuth2SignupRequest()
        oAuth2Service.signup(request, me.id!!)

        // when
        val response = memberQueryService.findMemberChatProfileFrom(me.id!!)

        // then
        assertThat(response)
            .extracting("profileImageUrl", "nickname")
            .contains(request.profileImageUrl, request.nickname)
    }

    @DisplayName("채팅방 회원 프로필 조회")
    @Test
    fun findMemberProfileFrom() {
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

        val other2 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(other2)
        val signupRequest3 = TestDataFactory.oAuth2SignupRequest(
            nickname = "updateNick",
            profileImageUrl = "updateProfile"
        )
        oAuth2Service.signup(signupRequest3, other2.id!!)


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

        val chatRoom2 = chatRoomRepository.save(ChatRoom(ChatRoomType.PRIVATE))

        val chatParticipant3 = ChatParticipant(
            chatRoom2,
            me
        )
        val chatParticipant4 = ChatParticipant(
            chatRoom2,
            other2
        )
        chatParticipantRepository.save(chatParticipant3)
        chatParticipantRepository.save(chatParticipant4)

        val chatRoomIds = listOf(chatRoom.id!!, chatRoom2.id!!)

        // when
        val findMembers = memberQueryService.findMemberProfileFrom(chatRoomIds)

        // then
        assertThat(findMembers).hasSize(4)
        assertThat(findMembers)
            .extracting("chatRoomId", "memberId", "nickname", "profileImageUrl")
            .containsExactlyInAnyOrder(
                tuple(chatRoom.id!!, me.id!!, signupRequest1.nickname, signupRequest1.profileImageUrl),
                tuple(chatRoom.id!!, other.id!!, signupRequest2.nickname, signupRequest2.profileImageUrl),
                tuple(chatRoom2.id!!, me.id!!, signupRequest1.nickname, signupRequest1.profileImageUrl),
                tuple(chatRoom2.id!!, other2.id!!, signupRequest3.nickname, signupRequest3.profileImageUrl),
            )
    }

    @DisplayName("특정 채팅방 회원 프로필 조회")
    @Test
    fun findMemberProfileFromByChatRoomId() {
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
        val findMembers = memberQueryService.findMemberProfileFrom(chatRoom.id!!)

        // then
        assertThat(findMembers).hasSize(2)
        assertThat(findMembers)
            .extracting("chatRoomId", "memberId", "nickname", "profileImageUrl")
            .containsExactlyInAnyOrder(
                tuple(chatRoom.id!!, me.id!!, signupRequest1.nickname, signupRequest1.profileImageUrl),
                tuple(chatRoom.id!!, other.id!!, signupRequest2.nickname, signupRequest2.profileImageUrl),
            )
    }
}