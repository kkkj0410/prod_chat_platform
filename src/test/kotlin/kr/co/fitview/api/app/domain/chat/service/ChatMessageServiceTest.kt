package kr.co.fitview.api.app.domain.chat.service

import jakarta.persistence.EntityManager
import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.dto.request.ChatTextMessageServiceRequest
import kr.co.fitview.api.app.domain.chat.entity.*
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.chat.repository.*
import kr.co.fitview.api.app.domain.fcm.dto.request.EventFcmChatMessage
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.global.stomp.dto.request.StompEventTextMessageDepth1
import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.domain.workout.repository.WorkoutRequestRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import kr.co.fitview.api.app.global.util.TestDataFactory
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ChatMessageServiceTest @Autowired constructor(
    val chatMessageService : ChatMessageService,
    val chatMessageRepository: ChatMessageRepository,
    val chatParticipantRepository: ChatParticipantRepository,
    val chatRoomRepository: ChatRoomRepository,
    val workoutRequestRepository: WorkoutRequestRepository,
    val messageReadStatusRepository: MessageReadStatusRepository,
    val oAuth2Service: OAuth2Service,
    val memberRepository: MemberRepository,
    val time: Time,
    val em : EntityManager
)
    : IntegrationTestSupport(){



    @DisplayName("회원은 TEXT 형태의 채팅 메시지를 보낸다.")
    @Test
    fun saveChatTextMessage() {
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

        val request = ChatTextMessageServiceRequest(
            content = "hello",
        )

        // when
        chatMessageService.saveChatTextMessage(
            member = me,
            chatRoom = savedChatRoom,
            message = request
        )

        // then
        val findChatMessage = chatMessageRepository.findAll()[0]

        assertThat(findChatMessage)
            .extracting("member", "chatRoom", "type", "content", "sentAt")
            .contains(me, savedChatRoom, ChatMessageType.TEXT, request.content, time.nowLocalDateTime)
    }


    @DisplayName("회원은 TEXT 형태의 채팅 메시지를 보내면 채팅방의 최신 시간이 갱신된다.")
    @Test
    fun saveChatTextMessageChatRoomLastMessageAt() {
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

        val request = ChatTextMessageServiceRequest(
            content = "hello",
        )

        // when
        chatMessageService.saveChatTextMessage(
            member = me,
            chatRoom = savedChatRoom,
            message = request
        )

        // then
        assertThat(savedChatRoom.lastMessageAt).isEqualTo(time.nowLocalDateTime)
    }

    @DisplayName("회원은 TEXT 형태의 채팅 메시지를 보낼 시, 문자 읽음 여부가 생성된다.")
    @Test
    fun saveChatTextMessageMessageReadStatus() {
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

        val request = ChatTextMessageServiceRequest(
            content = "hello",
        )

        // when
        chatMessageService.saveChatTextMessage(
            member = me,
            chatRoom = savedChatRoom,
            message = request
        )

        // then
        val findChatMessage = chatMessageRepository.findAll()[0]
        val findMessageReadStatuses = messageReadStatusRepository.findAll()

        assertThat(findMessageReadStatuses)
            .extracting("chatRoom", "member", "chatMessage", "isRead")
            .containsExactlyInAnyOrder(
                tuple(savedChatRoom, me, findChatMessage, true),
                tuple(savedChatRoom, other, findChatMessage, false),
            )
    }

    @DisplayName("회원은 TEXT 형태의 채팅 메시지를 보낼 시, 실시간 알람이 발생한다.")
    @Test
    fun saveChatTextMessageStomp() {
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

        val request = ChatTextMessageServiceRequest(
            content = "hello",
        )

        // when
        chatMessageService.saveChatTextMessage(
            member = me,
            chatRoom = savedChatRoom,
            message = request
        )

        // then
        val count = events.stream(StompEventTextMessageDepth1::class.java).count()
        assertThat(count).isEqualTo(2)
    }

    @DisplayName("회원은 TEXT 형태의 채팅 메시지를 보낼 시, 상대방에게 푸시 알람이 발생한다.")
    @Test
    fun saveChatTextMessageFcm() {
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

        val request = ChatTextMessageServiceRequest(
            content = "hello",
        )

        // when
        chatMessageService.saveChatTextMessage(
            member = me,
            chatRoom = savedChatRoom,
            message = request
        )

        // then
        val count = events.stream(EventFcmChatMessage::class.java).count()
        assertThat(count).isEqualTo(1)
    }


}