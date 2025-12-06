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

class ChatMessageServiceTest @Autowired constructor(
    val chatMessageService : ChatMessageService,
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
//    @Test
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


}