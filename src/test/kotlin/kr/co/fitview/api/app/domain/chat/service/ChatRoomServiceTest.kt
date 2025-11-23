package kr.co.fitview.api.app.domain.chat.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.condition.ChatCondition
import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.MessageReadStatus
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.chat.repository.ChatMessageRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatParticipantRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatRoomRepository
import kr.co.fitview.api.app.domain.chat.repository.MessageReadStatusRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.domain.workout.dto.response.enums.WorkoutRequestStatusForResponse
import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequest
import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import kr.co.fitview.api.app.domain.workout.repository.WorkoutRequestRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import kr.co.fitview.api.app.global.util.TestDataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ChatRoomServiceTest @Autowired constructor(
    private val chatRoomService: ChatRoomService,
    private val chatRoomRepository: ChatRoomRepository,
    private val chatParticipantRepository: ChatParticipantRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val messageReadStatusRepository: MessageReadStatusRepository,
    private val workoutRequestRepository: WorkoutRequestRepository,
    private val oAuth2Service: OAuth2Service,
    private val memberRepository: MemberRepository,
    private val time: Time
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

    @DisplayName("채팅방 목록을 조회한다.")
    @Test
    fun findChatRooms() {
        //given
        val me = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        val other1 = Member(
            email = "email2",
            password = "password2",
            role = Role.USER,
        )
        val other2 = Member(
            email = "email2",
            password = "password2",
            role = Role.USER,
        )
        memberRepository.save(me)
        memberRepository.save(other1)
        memberRepository.save(other2)

        val signupRequest = TestDataFactory.oAuth2SignupRequest()

        oAuth2Service.signup(signupRequest, me.id!!)
        oAuth2Service.signup(signupRequest, other1.id!!)
        oAuth2Service.signup(signupRequest, other2.id!!)

        val chatRoom1 = chatRoomRepository.save(ChatRoom(ChatRoomType.PRIVATE))
        val chatRoom2 = chatRoomRepository.save(ChatRoom(ChatRoomType.PRIVATE))

        val chatParticipant1 = ChatParticipant(
            chatRoom1,
            me
        )
        val chatParticipant2 = ChatParticipant(
            chatRoom1,
            other1
        )
        val chatParticipant3 = ChatParticipant(
            chatRoom2,
            me
        )
        val chatParticipant4 = ChatParticipant(
            chatRoom2,
            other2
        )
        chatParticipantRepository.save(chatParticipant1)
        chatParticipantRepository.save(chatParticipant2)
        chatParticipantRepository.save(chatParticipant3)
        chatParticipantRepository.save(chatParticipant4)

        val message1ByChatRoom1 = ChatMessage(
            member = me,
            chatRoom = chatRoom1,
            type = ChatMessageType.TEXT,
            content = "content",
            sentAt = time.nowLocalDateTime.minusHours(3)
        )
        val message2ByChatRoom1 = ChatMessage(
            member = me,
            chatRoom = chatRoom1,
            type = ChatMessageType.TEXT,
            content = "content",
            sentAt = time.nowLocalDateTime
        )
        val message1ByChatRoom2 = ChatMessage(
            member = other2,
            chatRoom = chatRoom2,
            type = ChatMessageType.WORKOUT_REQUEST,
            sentAt = time.nowLocalDateTime
        )
        val workout1ByChatRoom2 = WorkoutRequest(
            chatMessage = message1ByChatRoom2,
            fromMember = other2,
            toMember = me,
            status = WorkoutRequestStatus.PENDING,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusHours(24),
            requestedAt = time.nowLocalDateTime.minusHours(3)
        )
        chatMessageRepository.save(message1ByChatRoom1)
        chatMessageRepository.save(message2ByChatRoom1)
        chatMessageRepository.save(message1ByChatRoom2)

        workoutRequestRepository.save(workout1ByChatRoom2)

        val messageRead = MessageReadStatus(
            chatRoom = chatRoom2,
            member = me,
            chatMessage = message1ByChatRoom2,
            isRead = true
        )
        messageReadStatusRepository.save(messageRead)

        val condition = ChatCondition()

        // when
        val response = chatRoomService.findChatRooms(me.id!!, condition)
        val content = response.content

        //then
        assertThat(content[0])
            .extracting("chatRoomId", "profileImageUrl", "nickname", "isRead")
            .contains(chatRoom1.id!!, signupRequest.profileImageUrl, signupRequest.nickname, true)

        assertThat(content[1])
            .extracting("chatRoomId", "profileImageUrl", "nickname", "isRead")
            .contains(chatRoom2.id!!, signupRequest.profileImageUrl, signupRequest.nickname, true)

        assertThat(content[0].lastChatMessage)
            .extracting(
                "chatMessageId",
                "type",
                "sentAt",
                "isMe",
                "isRead",
                "chatRoomId",
                "memberId",
                "content"
            )
            .contains(
                message2ByChatRoom1.id!!,
                ChatMessageType.TEXT,
                time.nowLocalDateTime,
                true,
                true,
                chatRoom1.id!!,
                me.id!!,
                message2ByChatRoom1.content
            )

        assertThat(content[1].lastChatMessage)
            .extracting(
                "chatMessageId",
                "type",
                "sentAt",
                "isMe",
                "isRead",
                "chatRoomId",
                "memberId",
                "workoutRequestId",
                "status",
                "scheduledAt",
                "location"
            )
            .contains(
                message1ByChatRoom2.id!!,
                ChatMessageType.WORKOUT_REQUEST,
                time.nowLocalDateTime,
                false,
                true,
                chatRoom2.id!!,
                other2.id!!,
                workout1ByChatRoom2.id!!,
                WorkoutRequestStatusForResponse.PENDING,
                time.nowLocalDateTime.plusHours(24),
                "location"
            )

        assertThat(content[0].lastWorkoutRequest.status).isNull()
        assertThat(content[1].lastWorkoutRequest.status).isEqualTo(WorkoutRequestStatusForResponse.PENDING)

        assertThat(response.hasNext()).isEqualTo(false)
    }

}