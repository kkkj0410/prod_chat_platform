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
import kr.co.fitview.api.app.domain.review.entity.Review
import kr.co.fitview.api.app.domain.review.entity.enums.ReviewType
import kr.co.fitview.api.app.domain.review.repository.ReviewRepository
import kr.co.fitview.api.app.domain.workout.condition.AdminWorkoutRequestCondition
import kr.co.fitview.api.app.domain.workout.dto.response.enums.WorkoutRequestStatusForRequest
import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequest
import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequestLog
import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import kr.co.fitview.api.app.domain.workout.repository.WorkoutRequestLogRepository
import kr.co.fitview.api.app.domain.workout_history.repository.WorkoutHistoryRepository
import kr.co.fitview.api.app.domain.workout.repository.WorkoutRequestRepository
import kr.co.fitview.api.app.domain.workout_history.entity.WorkoutHistory
import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartner
import kr.co.fitview.api.app.domain.workout_partner.repository.WorkoutPartnerRepository
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
    val workoutPartnerRepository : WorkoutPartnerRepository,
    val workoutRequestLogRepository: WorkoutRequestLogRepository,
    val reviewRepository : ReviewRepository,
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

    @DisplayName("전체 운동 요청을 조회한다.")
    @Test
    fun findAllWorkoutRequestBy() {
        // given
        val member1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        val member2 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        val member3 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member1)
        memberRepository.save(member2)
        memberRepository.save(member3)

        val signupRequest = TestDataFactory.oAuth2SignupRequest()
        oAuth2Service.signup(signupRequest, member1.id!!)
        oAuth2Service.signup(signupRequest, member2.id!!)
        oAuth2Service.signup(signupRequest, member3.id!!)

        val workoutPartner1 = WorkoutPartner(
            memberOne = member1,
            memberTwo = member2
        )
        val workoutPartner2 = WorkoutPartner(
            memberOne = member1,
            memberTwo = member3
        )
        workoutPartnerRepository.save(workoutPartner1)
        workoutPartnerRepository.save(workoutPartner2)

        val chatRoom1 = chatRoomRepository.save(ChatRoom(ChatRoomType.PRIVATE))
        val chatRoom2 = chatRoomRepository.save(ChatRoom(ChatRoomType.PRIVATE))

        val chatParticipant1 = ChatParticipant(
            chatRoom1,
            member1
        )
        val chatParticipant2 = ChatParticipant(
            chatRoom1,
            member2
        )
        val chatParticipant3 = ChatParticipant(
            chatRoom2,
            member1
        )
        val chatParticipant4 = ChatParticipant(
            chatRoom2,
            member3
        )
        chatParticipantRepository.save(chatParticipant1)
        chatParticipantRepository.save(chatParticipant2)
        chatParticipantRepository.save(chatParticipant3)
        chatParticipantRepository.save(chatParticipant4)

        val chatMessage1 = ChatMessage.ofWorkoutRequest(
            member = member1,
            chatRoom = chatRoom1,
            sentAt = time.nowLocalDateTime
        )
        val chatMessage2 = ChatMessage.ofWorkoutRequest(
            member = member3,
            chatRoom = chatRoom2,
            sentAt = time.nowLocalDateTime.plusHours(5)
        )
        chatMessageRepository.save(chatMessage1)
        chatMessageRepository.save(chatMessage2)

        val workoutRequest1 = WorkoutRequest.of(
            chatMessage = chatMessage1,
            fromMember = member1,
            toMember = member2,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        val workoutRequest2 = WorkoutRequest.of(
            chatMessage = chatMessage2,
            fromMember = member3,
            toMember = member1,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime.plusHours(5)
        )
        workoutRequestRepository.save(workoutRequest1)
        workoutRequestRepository.save(workoutRequest2)

        val log1 = WorkoutRequestLog(
            workoutRequest = workoutRequest1,
            status = WorkoutRequestStatus.PENDING,
            loggedAt = time.nowLocalDateTime
        )
        val log1_1 = WorkoutRequestLog(
            workoutRequest = workoutRequest1,
            status = WorkoutRequestStatus.COMPLETE,
            loggedAt = time.nowLocalDateTime.plusHours(10)
        )
        val log2 = WorkoutRequestLog(
            workoutRequest = workoutRequest2,
            status = WorkoutRequestStatus.PENDING,
            loggedAt = time.nowLocalDateTime.plusHours(5)
        )
        workoutRequestLogRepository.save(log1)
        workoutRequestLogRepository.save(log1_1)
        workoutRequestLogRepository.save(log2)

        val workoutHistory1 = WorkoutHistory(
            chatRoom = chatRoom1,
            workoutRequest = workoutRequest1,
            memberOne = member1,
            memberTwo = member2,
            completedAt = time.nowLocalDateTime.plusHours(10)
        )
        workoutHistoryRepository.save(workoutHistory1)

        val review1 = Review(
            fromMember = member1,
            toMember = member2,
            workoutHistory = workoutHistory1,
            isPrivate = false,
            ReviewType.GOOD,
            score = 2.0,
            content = null,
            postedAt = time.nowLocalDateTime.plusHours(15)
        )
        reviewRepository.save(review1)

        val condition = AdminWorkoutRequestCondition()

        // when
        val response = workoutRequestQueryService.findAllWorkoutRequestFrom(condition)

        // then
        assertThat(response)
            .extracting("workoutPartnerId", "workoutRequestId", "respondedAt", "hasFromMemberReview", "hasToMemberReview")
            .containsExactly(
                tuple(
                    workoutPartner2.id!!,
                    workoutRequest2.id!!,
                    time.nowLocalDateTime.plusHours(5),
                    false,
                    false
                ),
                tuple(
                    workoutPartner1.id!!,
                    workoutRequest1.id!!,
                    time.nowLocalDateTime.plusHours(10),
                    true,
                    false
                )
            )
    }
}