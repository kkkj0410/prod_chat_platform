package kr.co.fitview.api.app.domain.chat.service

import jakarta.persistence.EntityManager
import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.condition.ChatMessageCondition
import kr.co.fitview.api.app.domain.chat.dto.request.ChatTextMessageServiceRequest
import kr.co.fitview.api.app.domain.chat.dto.request.ChatWorkoutRequestMessageServiceRequest
import kr.co.fitview.api.app.domain.chat.entity.*
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatNoticeMessageType
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.chat.repository.*
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

class ChatMessageQueryServiceTest @Autowired constructor(
    val chatMessageQueryService : ChatMessageQueryService,
    val chatMessageRepository: ChatMessageRepository,
    val chatParticipantRepository: ChatParticipantRepository,
    val chatRoomRepository: ChatRoomRepository,
    val workoutRequestRepository: WorkoutRequestRepository,
    val messageReadStatusRepository: MessageReadStatusRepository,
    val oAuth2Service: OAuth2Service,
    val memberRepository: MemberRepository,
    val chatNoticeMessageRepository : ChatNoticeMessageRepository,
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
        val response = chatMessageQueryService.findLastChatMessages(me.id!!, chatRoomIds)


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

        val condition = ChatMessageCondition(
            size = 10,
        )

        // when
        val slice = chatMessageQueryService.findChatMessages(me.id!!, chatRoom.id!!, condition)
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

        val condition = ChatMessageCondition(
            size = 10,
        )

        // when & then
        assertThatThrownBy {
            chatMessageQueryService.findChatMessages(555L, chatRoom.id!!, condition)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(ChatErrorCode.NOT_MEMBER_OF_CHAT_ROOM)
            })
    }

    @DisplayName("안내 문구 메시지를 조회한다.")
    @Test
    fun findChatMessagesNotice() {
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


        val chatMessage1 = ChatMessage.ofNotice(
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime
        )
        val chatNoticeMessage1 = ChatNoticeMessage(
            chatMessage = chatMessage1,
            type = ChatNoticeMessageType.WORKOUT_REQUEST_COMPLETE
        )
        chatMessageRepository.save(chatMessage1)
        chatNoticeMessageRepository.save(chatNoticeMessage1)


        val condition = ChatMessageCondition(
            size = 10,
        )

        // when
        val slice = chatMessageQueryService.findChatMessages(me.id!!, chatRoom.id!!, condition)
        val response = slice.content

        // then
        assertThat(response).hasSize(1)
        assertThat(response[0])
            .extracting("chatMessageId", "type", "sentAt", "content")
            .contains(chatMessage1.id, ChatMessageType.NOTICE, chatMessage1.sentAt, ChatNoticeMessageType.WORKOUT_REQUEST_COMPLETE)
    }

}