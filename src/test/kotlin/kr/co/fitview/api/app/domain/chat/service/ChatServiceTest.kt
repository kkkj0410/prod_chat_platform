package kr.co.fitview.api.app.domain.chat.service

import com.fasterxml.jackson.annotation.JsonIgnore
import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.dto.request.ChatRoomCreateServiceRequest
import kr.co.fitview.api.app.domain.chat.dto.request.ChatTextMessageRequest
import kr.co.fitview.api.app.domain.chat.dto.request.ChatWorkoutRequestMessageRequest
import kr.co.fitview.api.app.domain.chat.dto.response.ChatMessageContent
import kr.co.fitview.api.app.domain.chat.entity.ChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.chat.repository.ChatMessageRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatParticipantRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatRoomRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.domain.workout.dto.response.enums.WorkoutRequestStatusFor
import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import kr.co.fitview.api.app.domain.workout.repository.WorkoutRequestRepository
import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartner
import kr.co.fitview.api.app.domain.workout_partner.repository.WorkoutPartnerRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.chat.ChatErrorCode
import kr.co.fitview.api.app.global.time.Time
import kr.co.fitview.api.app.global.util.TestDataFactory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.ThrowingConsumer
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

class ChatServiceTest@Autowired constructor(
    val chatService: ChatService,
    val chatRoomRepository : ChatRoomRepository,
    val chatParticipantRepository : ChatParticipantRepository,
    val chatMessageRepository : ChatMessageRepository,
    val workoutPartnerRepository : WorkoutPartnerRepository,
    val workoutRequestRepository : WorkoutRequestRepository,
    val oAuth2Service : OAuth2Service,
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
    @Test
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

        val workoutPartner = WorkoutPartner.of(savedMember1, savedMember2)
        workoutPartnerRepository.save(workoutPartner)

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

    @DisplayName("개인 채팅방을 생성하려는데 핏버디가 성사 되어있지 않으면 채팅방 생성을 하지 않는다.")
    @Test
    fun saveChatRoomNotWorkoutPartner() {
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

        val request = ChatRoomCreateServiceRequest(savedMember2.id!!)

        // when & then
        assertThatThrownBy {
            chatService.saveChatRoom(savedMember1.id!!, request)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(ChatErrorCode.NOT_PARTNER)
            })
    }

    @DisplayName("채팅방에 TEXT 메시지를 보낸다.")
    @Test
    fun sendMessage() {
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

        val signupRequest = TestDataFactory.oAuth2SignupRequest()
        oAuth2Service.signup(signupRequest, me.id!!)

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

        val request = ChatTextMessageRequest(
            content = "hello"
        )

        // when
        val response = chatService.sendMessage(
            memberId = me.id!!,
            chatRoomId = savedChatRoom.id!!,
            message = request
        )

        // then
        val findChatMessage = chatMessageRepository.findAll()[0]

        assertThat(response)
            .extracting(
                "chatRoomId",
                "profileImageUrl",
                "nickname",
                "chatMessage.chatMessageId",
                "chatMessage.type",
                "chatMessage.sentAt",
                "chatMessage.content",
            )
            .contains(
                savedChatRoom.id!!,
                signupRequest.profileImageUrl,
                signupRequest.nickname,
                findChatMessage.id!!,
                ChatMessageType.TEXT,
                time.nowLocalDateTime,
                request.content
            )

        assertThat(findChatMessage)
            .extracting("member", "chatRoom", "type", "content", "sentAt")
            .contains(me, savedChatRoom, ChatMessageType.TEXT, request.content, time.nowLocalDateTime)

    }

    @DisplayName("채팅방에 WORKOUT_REQUEST 메시지를 보낸다.")
    @Test
    fun sendMessageWorkoutRequest() {
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

        val signupRequest = TestDataFactory.oAuth2SignupRequest()
        oAuth2Service.signup(signupRequest, me.id!!)

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

        val request = ChatWorkoutRequestMessageRequest(
            scheduledAt = time.nowLocalDateTime.plusHours(5),
            location = "location"
        )

        // when
        val response = chatService.sendMessage(
            memberId = me.id!!,
            chatRoomId = savedChatRoom.id!!,
            message = request
        )

        // then
        val findChatMessage = chatMessageRepository.findAll()[0]
        val findWorkoutRequest = workoutRequestRepository.findAll()[0]
//
        assertThat(response)
            .extracting(
                "chatRoomId",
                "profileImageUrl",
                "nickname",
                "chatMessage.chatMessageId",
                "chatMessage.type",
                "chatMessage.sentAt",
                "chatMessage.workoutRequestId",
                "chatMessage.status",
                "chatMessage.scheduledAt",
                "chatMessage.location"
            )
            .contains(
                savedChatRoom.id!!,
                signupRequest.profileImageUrl,
                signupRequest.nickname,
                findChatMessage.id!!,
                ChatMessageType.WORKOUT_REQUEST,
                time.nowLocalDateTime,
                findWorkoutRequest.id!!,
                WorkoutRequestStatusFor.PENDING,
                request.scheduledAt,
                request.location
            )


        assertThat(findChatMessage)
            .extracting("member", "chatRoom", "type", "content", "sentAt")
            .contains(me, savedChatRoom, ChatMessageType.WORKOUT_REQUEST, null, time.nowLocalDateTime)

        assertThat(findWorkoutRequest)
            .extracting("fromMember", "toMember", "status", "location", "scheduledAt", "requestedAt")
            .contains(
                me,
                other,
                WorkoutRequestStatus.PENDING,
                request.location,
                request.scheduledAt,
                time.nowLocalDateTime
            )
    }

    @DisplayName("해당 회원이 지정 채팅방에 있는 것이 조회되지 않으면 메시지를 보내지 않는다.")
    @Test
    fun sendMessageInvalidChatRoom() {
        // given
        val request = ChatTextMessageRequest(
            content = "hello"
        )

        // when & then
        assertThatThrownBy {
            chatService.sendMessage(
                memberId = 1L,
                chatRoomId = 1L,
                message = request
            )        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(ChatErrorCode.NOT_MEMBER_OF_CHAT_ROOM)
            })
    }



}