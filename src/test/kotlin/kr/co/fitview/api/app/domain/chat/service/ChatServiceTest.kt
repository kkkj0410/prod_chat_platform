package kr.co.fitview.api.app.domain.chat.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.auth.repository.RefreshTokenRepository
import kr.co.fitview.api.app.domain.auth.service.AuthService
import kr.co.fitview.api.app.domain.auth.service.RefreshTokenService
import kr.co.fitview.api.app.domain.chat.dto.request.ChatRoomCreateServiceRequest
import kr.co.fitview.api.app.domain.chat.entity.ChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.chat.repository.ChatParticipantRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatRoomRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartner
import kr.co.fitview.api.app.domain.workout_partner.repository.WorkoutPartnerRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.chat.ChatErrorCode
import kr.co.fitview.api.app.global.exception.error.member.MemberErrorCode
import kr.co.fitview.api.app.global.jwt.JwtTokenProvider
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.ThrowingConsumer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ChatServiceTest@Autowired constructor(
    val chatService: ChatService,
    val chatRoomRepository : ChatRoomRepository,
    val chatParticipantRepository : ChatParticipantRepository,
    val workoutPartnerRepository : WorkoutPartnerRepository,
    val memberRepository : MemberRepository,
    val time : Time
) : IntegrationTestSupport() {

    @DisplayName("개인 채팅방을 생성한다.")
    @Test
    fun saveChatRoom() {
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

        val workoutPartner = WorkoutPartner.of(savedMember1, savedMember2)
        workoutPartnerRepository.save(workoutPartner)

        val request = ChatRoomCreateServiceRequest(savedMember2.id!!)

        // when
        val response = chatService.saveChatRoom(savedMember1.id!!, request)

        // then
        val findChatRoom = chatRoomRepository.findPrivateChatRoomIdBetweenMemberIds(savedMember1.id!!, savedMember2.id!!)
        assertThat(response.chatRoomId).isEqualTo(findChatRoom!!.id!!)
    }

    @DisplayName("두 회원 간의 개인 채팅방이 이미 있으면 해당 채팅방을 조회한다.")
//    @Test
    fun saveChatRoomWhenAlreadyChatRoom() {
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

        val request = ChatRoomCreateServiceRequest(savedMember2.id!!)

        val findChatRoom = chatRoomRepository.findPrivateChatRoomIdBetweenMemberIds(
            member1.id!!, member2.id!!
        )

        // when
        val response = chatService.saveChatRoom(savedMember1.id!!, request)

        // then
        assertThat(response.chatRoomId).isEqualTo(findChatRoom!!.id!!)
    }

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
        val findChatRoom = chatService.findChatRoomFrom(member1.id!!, member2.id!!)

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
        val findChatRoom = chatService.findChatRoomFrom(member1.id!!, member2.id!!)

        // then
        assertThat(findChatRoom).isNull()
    }

//    @DisplayName("개인 채팅방을 생성하려는데 핏버디가 성사 되어있지 않으면 채팅방 생성을 하지 않는다.")
//    @Test
//    fun saveChatRoomNotWorkoutPartner() {
//        // given
//        val member1 = Member(
//            email = "email1",
//            password = "password1",
//            role = Role.USER,
//        )
//        val member2 = Member(
//            email = "email2",
//            password = "password2",
//            role = Role.USER,
//        )
//        val savedMember1 = memberRepository.save(member1)
//        val savedMember2 = memberRepository.save(member2)
//
//        val request = ChatRoomCreateServiceRequest(savedMember2.id!!)
//
//        // when & then
//        assertThatThrownBy {
//            chatService.saveChatRoom(savedMember1.id!!, request)
//        }
//            .isInstanceOf(GlobalException::class.java)
//            .satisfies(ThrowingConsumer { ex ->
//                val globalEx = ex as GlobalException
//                assertThat(globalEx.errorCode)
//                    .isEqualTo(ChatErrorCode.NOT_PARTNER)
//            })
//    }
}