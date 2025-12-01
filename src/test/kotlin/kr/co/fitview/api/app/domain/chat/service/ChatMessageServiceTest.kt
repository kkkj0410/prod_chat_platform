package kr.co.fitview.api.app.domain.chat.service

import jakarta.persistence.EntityManager
import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.condition.ChatCondition
import kr.co.fitview.api.app.domain.chat.dto.request.ChatTextMessageServiceRequest
import kr.co.fitview.api.app.domain.chat.dto.request.ChatWorkoutRequestMessageServiceRequest
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
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.chat.ChatErrorCode
import kr.co.fitview.api.app.global.time.Time
import kr.co.fitview.api.app.global.util.TestDataFactory
import org.assertj.core.api.Assertions.*
import org.assertj.core.api.ThrowingConsumer
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

    @DisplayName("채팅방의 마지막 채팅을 가져온다")
    @Test
    fun findLastChatMessages() {
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

        val chatRoomIds = listOf(chatRoom1.id!!, chatRoom2.id!!)

        // when
        val response = chatMessageService.findLastChatMessages(me.id!!, chatRoomIds)


        assertThat(response[0])
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

        assertThat(response[1])
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
    }

    @DisplayName("채팅방의 메시지들을 조회한다.")
    @Test
    fun findChatMessages() {
        //given
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


        val chatMessage1 = ChatMessage(
            member = me,
            chatRoom = chatRoom,
            type = ChatMessageType.TEXT,
            content = "content",
            sentAt = time.nowLocalDateTime.minusHours(20)
        )
        chatMessageRepository.save(chatMessage1)

        val chatMessage2 = ChatMessage(
            member = me,
            chatRoom = chatRoom,
            type = ChatMessageType.WORKOUT_REQUEST,
            sentAt = time.nowLocalDateTime.minusHours(10)
        )
        val workoutRequest = WorkoutRequest(
            chatMessage = chatMessage2,
            fromMember = other,
            toMember = me,
            status = WorkoutRequestStatus.PENDING,
            scheduledAt = time.nowLocalDateTime.plusHours(24),
            requestedAt = time.nowLocalDateTime.minusHours(10),
            location = "location"
        )
        chatMessageRepository.save(chatMessage2)
        workoutRequestRepository.save(workoutRequest)

        val condition = ChatCondition(
            size = 10,
        )

        // when
        val slice = chatMessageService.findChatMessages(me.id!!, chatRoom.id!!, condition)
        val response = slice.content

        // then
        assertThat(response[1])
            .extracting("chatMessageId", "type", "sentAt", "isMe", "content")
            .contains(chatMessage1.id, chatMessage1.type, chatMessage1.sentAt, true, chatMessage1.content)

        assertThat(response[0])
            .extracting("chatMessageId", "sentAt", "isMe", "workoutRequestId", "status", "scheduledAt", "location")
            .contains(
                chatMessage2.id,
                chatMessage2.sentAt,
                true,
                workoutRequest.id!!,
                WorkoutRequestStatusForResponse.PENDING,
                workoutRequest.scheduledAt,
                workoutRequest.location
            )
    }

    @DisplayName("채팅방을 쓰지 않는 회원은 해당 채팅방의 내용을 열람할 수 없다.")
    @Test
    fun findChatMessagesOtherMember() {
        //given
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


        val chatMessage1 = ChatMessage(
            member = me,
            chatRoom = chatRoom,
            type = ChatMessageType.TEXT,
            content = "content",
            sentAt = time.nowLocalDateTime.minusHours(20)
        )
        chatMessageRepository.save(chatMessage1)

        val chatMessage2 = ChatMessage(
            member = me,
            chatRoom = chatRoom,
            type = ChatMessageType.WORKOUT_REQUEST,
            sentAt = time.nowLocalDateTime.minusHours(10)
        )
        val workoutRequest = WorkoutRequest(
            chatMessage = chatMessage2,
            fromMember = other,
            toMember = me,
            status = WorkoutRequestStatus.PENDING,
            scheduledAt = time.nowLocalDateTime.plusHours(24),
            requestedAt = time.nowLocalDateTime.minusHours(10),
            location = "location"
        )
        chatMessageRepository.save(chatMessage2)
        workoutRequestRepository.save(workoutRequest)

        val condition = ChatCondition(
            size = 10,
        )

        // when & then
        assertThatThrownBy {
            chatMessageService.findChatMessages(555L, chatRoom.id!!, condition)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(ChatErrorCode.NOT_MEMBER_OF_CHAT_ROOM)
            })
    }


    @DisplayName("회원은 TEXT 형태의 채팅 메시지를 보낸다.")
    @Test
    fun addChatTextMessage() {
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
            content = "hello"
        )

        // when
        chatMessageService.addChatTextMessage(
            member = me,
            chatRoom = savedChatRoom,
            message = request
        )

        // then
        val findChatMessage = chatMessageRepository.findAll()[0]
        val findMessageReadStatuses = messageReadStatusRepository.findAll()

        assertThat(findChatMessage)
            .extracting("member", "chatRoom", "type", "content", "sentAt")
            .contains(me, savedChatRoom, ChatMessageType.TEXT, request.content, time.nowLocalDateTime)

        assertThat(savedChatRoom.lastMessageAt).isEqualTo(time.nowLocalDateTime)

        assertThat(findMessageReadStatuses)
            .extracting("chatRoom", "member", "chatMessage", "isRead")
            .containsExactlyInAnyOrder(
                tuple(savedChatRoom, me, findChatMessage, true),
                tuple(savedChatRoom, other, findChatMessage, false),
            )
    }

    @DisplayName("회원은 WORKOUT_REQUEST 메시지를 보낸다.")
    @Test
    fun addChatWorkoutRequestMessage() {
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

        val request = ChatWorkoutRequestMessageServiceRequest(
            scheduledAt = time.nowLocalDateTime.plusHours(5),
            location = "location"
        )

        // when
        chatMessageService.addChatWorkoutRequestMessage(
            fromMember = me,
            chatRoom = savedChatRoom,
            message = request
        )

        // then
        val findChatMessage = chatMessageRepository.findAll()[0]
        val findWorkoutRequest = workoutRequestRepository.findAll()[0]
        val findMessageReadStatuses = messageReadStatusRepository.findAll()

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

        assertThat(savedChatRoom.lastMessageAt).isEqualTo(time.nowLocalDateTime)

        assertThat(findMessageReadStatuses)
            .extracting("chatRoom", "member", "chatMessage", "isRead")
            .containsExactlyInAnyOrder(
                tuple(savedChatRoom, me, findChatMessage, true),
                tuple(savedChatRoom, other, findChatMessage, false),
            )
    }

    @DisplayName("회원은 운동 요청 메시지를 보내는데 이미 운동 요청이 활성화되어있으면 전송을 하지 않는다.")
    @Test
    fun addChatWorkoutRequestMessageWhenExistsWorkoutRequest() {
        // given
        val me = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        val other = Member(
            email = "email1",
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

        val chatMessage1 = ChatMessage.ofWorkoutRequest(
            member = me,
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage1)

        val workoutRequest = WorkoutRequest.of(
            chatMessage = chatMessage1,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        workoutRequest.updateStatus(WorkoutRequestStatus.PENDING)
        workoutRequestRepository.save(workoutRequest)

        val chatMessage2 = ChatMessage.ofWorkoutRequest(
            member = me,
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage2)

        val request = ChatWorkoutRequestMessageServiceRequest(
            scheduledAt = time.nowLocalDateTime.plusHours(5),
            location = "location"
        )

        // when & then
        assertThatThrownBy {
            chatMessageService.addChatWorkoutRequestMessage(
                fromMember = me,
                chatRoom = chatRoom,
                message = request
            )
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(ChatErrorCode.EXISTING_WORKOUT_REQUEST)
            })
    }

    @DisplayName("채팅방 내부 메시지와 상대방 프로필을 조회한다.")
    @Test
    fun findChatMessagesWithOtherMember() {
        //given
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
        oAuth2Service.signup(signupRequest, other.id!!)

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


        val chatMessage1 = ChatMessage(
            member = me,
            chatRoom = chatRoom,
            type = ChatMessageType.TEXT,
            content = "content",
            sentAt = time.nowLocalDateTime.minusHours(20)
        )
        chatMessageRepository.save(chatMessage1)

        val chatMessage2 = ChatMessage(
            member = me,
            chatRoom = chatRoom,
            type = ChatMessageType.WORKOUT_REQUEST,
            sentAt = time.nowLocalDateTime.minusHours(10)
        )
        val workoutRequest = WorkoutRequest(
            chatMessage = chatMessage2,
            fromMember = other,
            toMember = me,
            status = WorkoutRequestStatus.PENDING,
            scheduledAt = time.nowLocalDateTime.plusHours(24),
            requestedAt = time.nowLocalDateTime.minusHours(10),
            location = "location"
        )
        chatMessageRepository.save(chatMessage2)
        workoutRequestRepository.save(workoutRequest)

        val condition = ChatCondition(
            size = 10,
        )

        // when
        val response = chatMessageService.findChatMessagesWithOtherMember(me.id!!, chatRoom.id!!, condition)

        val memberResponse = response.otherMember
        val chatMessageResponse = response.chatMessages.content

        // then
        assertThat(memberResponse)
            .extracting("memberId", "nickname", "profileImageUrl")
            .contains(other.id!!, other.nickname, signupRequest.profileImageUrl)

        assertThat(chatMessageResponse[1])
            .extracting("chatMessageId", "type", "sentAt", "isMe", "content")
            .contains(chatMessage1.id, chatMessage1.type, chatMessage1.sentAt, true, chatMessage1.content)

        assertThat(chatMessageResponse[0])
            .extracting("chatMessageId", "sentAt", "isMe", "workoutRequestId", "status", "scheduledAt", "location")
            .contains(
                chatMessage2.id,
                chatMessage2.sentAt,
                true,
                workoutRequest.id!!,
                WorkoutRequestStatusForResponse.PENDING,
                workoutRequest.scheduledAt,
                workoutRequest.location
            )
    }

}