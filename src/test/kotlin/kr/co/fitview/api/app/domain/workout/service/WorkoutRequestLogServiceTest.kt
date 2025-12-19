package kr.co.fitview.api.app.domain.workout.service

import jakarta.persistence.EntityManager
import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.dto.request.ChatWorkoutRequestMessageServiceRequest
import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.QChatMessage.chatMessage
import kr.co.fitview.api.app.domain.chat.entity.QChatRoom.chatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.chat.repository.ChatMessageRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatParticipantRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatRoomRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequest
import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import kr.co.fitview.api.app.domain.workout.repository.WorkoutRequestRepository
import kr.co.fitview.api.app.domain.workout_history.repository.WorkoutHistoryRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import kr.co.fitview.api.app.global.util.TestDataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class WorkoutRequestLogServiceTest @Autowired constructor(
    val workoutRequestRepository : WorkoutRequestRepository,
    val workoutHistoryRepository : WorkoutHistoryRepository,
    val memberRepository : MemberRepository,
    val chatRoomRepository : ChatRoomRepository,
    val chatParticipantRepository : ChatParticipantRepository,
    val chatMessageRepository : ChatMessageRepository,
    val oAuth2Service : OAuth2Service,
    val workoutRequestLogService : WorkoutRequestLogService,
    val time : Time,
    val em : EntityManager
) : IntegrationTestSupport() {

    @DisplayName("운동 로그를 기록한다.")
    @Test
    fun addWorkoutRequestLog() {
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

        val chatMessage = ChatMessage.ofWorkoutRequest(
            member = me,
            chatRoom = savedChatRoom,
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage)

        val workoutRequest = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        workoutRequestRepository.save(workoutRequest)

        // when
        val savedWorkoutRequestLog = workoutRequestLogService.addWorkoutRequestLog(
            workoutRequestId = workoutRequest.id!!,
            status = WorkoutRequestStatus.PENDING
        )

        // then
        assertThat(savedWorkoutRequestLog.id).isNotNull()
        assertThat(savedWorkoutRequestLog)
            .extracting("workoutRequest", "status")
            .contains(
                workoutRequest,
                WorkoutRequestStatus.PENDING
            )

    }

    @DisplayName("운동 로그 저장 시, 여러 운동 요청에 대해서 한 번에 로그를 저장한다.")
    @Test
    fun addAllWorkoutRequestLog() {
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

        val chatMessage = ChatMessage.ofWorkoutRequest(
            member = me,
            chatRoom = savedChatRoom,
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage)

        val workoutRequest = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        workoutRequestRepository.save(workoutRequest)

        val workoutRequest2 = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        workoutRequestRepository.save(workoutRequest2)

        val workoutRequestIds = listOf(workoutRequest.id!!, workoutRequest2.id!!)

        // when
        val savedWorkoutRequestLog = workoutRequestLogService.addAllWorkoutRequestLog(
            workoutRequestIds = workoutRequestIds,
            status = WorkoutRequestStatus.EXPIRE
        )

        // then
        assertThat(savedWorkoutRequestLog).hasSize(2)

        assertThat(savedWorkoutRequestLog[0].id).isNotNull()
        assertThat(savedWorkoutRequestLog[0])
            .extracting("workoutRequest", "status")
            .contains(
                workoutRequest,
                WorkoutRequestStatus.EXPIRE
            )

        assertThat(savedWorkoutRequestLog[1].id).isNotNull()
        assertThat(savedWorkoutRequestLog[1])
            .extracting("workoutRequest", "status")
            .contains(
                workoutRequest2,
                WorkoutRequestStatus.EXPIRE
            )
    }

}