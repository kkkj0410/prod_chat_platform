package kr.co.fitview.api.app.domain.workout.service

import jakarta.persistence.EntityManager
import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.dto.request.ChatWorkoutRequestMessageServiceRequest
import kr.co.fitview.api.app.domain.chat.dto.request.WorkoutRequestUpdateRequest
import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatNoticeMessageType
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.chat.repository.ChatMessageRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatNoticeMessageRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatParticipantRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatRoomRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.domain.workout.dto.response.enums.WorkoutRequestStatusForRequest
import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequest
import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import kr.co.fitview.api.app.domain.workout_history.repository.WorkoutHistoryRepository
import kr.co.fitview.api.app.domain.workout.repository.WorkoutRequestRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.chat.ChatErrorCode
import kr.co.fitview.api.app.global.exception.error.workout_request.WorkoutRequestErrorCode
import kr.co.fitview.api.app.global.time.Time
import kr.co.fitview.api.app.global.util.TestDataFactory
import org.assertj.core.api.Assertions.*
import org.assertj.core.api.ThrowingConsumer
import org.hibernate.proxy.HibernateProxy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired

class WorkoutRequestQueryServiceTest @Autowired constructor(
    val workoutRequestRepository : WorkoutRequestRepository,
    val workoutRequestQueryService: WorkoutRequestQueryService,
    val workoutHistoryRepository : WorkoutHistoryRepository,
    val memberRepository : MemberRepository,
    val chatRoomRepository : ChatRoomRepository,
    val chatParticipantRepository : ChatParticipantRepository,
    val chatMessageRepository : ChatMessageRepository,
    val oAuth2Service : OAuth2Service,
    val time : Time,
    val em : EntityManager
) : IntegrationTestSupport() {


    @DisplayName("채팅방의 제일 최큰 운동 요청을 확인한다.")
    @Test
    fun findRecentWorkoutRequestFrom() {
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

        val chatMessage = ChatMessage.ofWorkoutRequest(
            member = me,
            chatRoom = chatRoom,
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
        val findWorkoutRequest = workoutRequestQueryService.findRecentWorkoutRequestFrom(chatRoom.id!!)

        // then
        assertThat(findWorkoutRequest)
            .extracting("chatMessage", "status")
            .contains(
                chatMessage,
                WorkoutRequestStatus.PENDING
            )
    }

    @DisplayName("채팅방의 제일 최큰 운동 요청을 조회하되, 운동 요청이 아예 없으면 조회하지 않는다.")
    @Test
    fun findRecentWorkoutRequestFromWhenNull() {
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

        // when
        val findWorkoutRequest = workoutRequestQueryService.findRecentWorkoutRequestFrom(chatRoom.id!!)

        // then
        assertThat(findWorkoutRequest).isNull()
    }


    @DisplayName("운동 요청을 프록시로 조회한다.")
    @Test
    fun findWorkoutRequestReferenceFrom() {
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

        val chatMessage = ChatMessage.ofWorkoutRequest(
            member = me,
            chatRoom = chatRoom,
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

        em.flush()
        em.clear()

        // when
        val workoutRequestProxy = workoutRequestQueryService.findWorkoutRequestReferenceFrom(workoutRequest.id!!)

        // then
        assertThat(workoutRequestProxy).isInstanceOf(HibernateProxy::class.java)
        assertThat(workoutRequestProxy::class.simpleName!!).contains("WorkoutRequest")
    }

    @DisplayName("운동 요청 id를 리스트로 받아서 프록시 리스트로 제작한다.")
    @Test
    fun findWorkoutRequestReferenceFromAll() {
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

        val chatMessage = ChatMessage.ofWorkoutRequest(
            member = me,
            chatRoom = chatRoom,
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

        em.flush()
        em.clear()

        // when
        val workoutRequestProxy = workoutRequestQueryService.findWorkoutRequestReferenceFrom(workoutRequestIds)

        // then
        assertThat(workoutRequestProxy).hasSize(2)

        assertThat(workoutRequestProxy[0]).isInstanceOf(HibernateProxy::class.java)
        assertThat(workoutRequestProxy[0]::class.simpleName!!).contains("WorkoutRequest")

        assertThat(workoutRequestProxy[1]).isInstanceOf(HibernateProxy::class.java)
        assertThat(workoutRequestProxy[1]::class.simpleName!!).contains("WorkoutRequest")

    }

}