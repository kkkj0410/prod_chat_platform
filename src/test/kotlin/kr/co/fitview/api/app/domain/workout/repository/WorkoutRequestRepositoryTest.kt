package kr.co.fitview.api.app.domain.workout.repository

import jakarta.persistence.EntityManager
import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
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
import kr.co.fitview.api.app.domain.review.entity.Review
import kr.co.fitview.api.app.domain.review.entity.enums.ReviewType
import kr.co.fitview.api.app.domain.review.repository.ReviewRepository
import kr.co.fitview.api.app.domain.review.service.ReviewService
import kr.co.fitview.api.app.domain.workout.condition.AdminWorkoutRequestCondition
import kr.co.fitview.api.app.domain.workout.dto.response.enums.WorkoutRequestStatusForResponse
import kr.co.fitview.api.app.domain.workout.entity.QWorkoutRequest.workoutRequest
import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequest
import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequestLog
import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import kr.co.fitview.api.app.domain.workout_history.entity.WorkoutHistory
import kr.co.fitview.api.app.domain.workout_history.repository.WorkoutHistoryRepository
import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartner
import kr.co.fitview.api.app.domain.workout_partner.repository.WorkoutPartnerRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import kr.co.fitview.api.app.global.util.TestDataFactory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.hibernate.Hibernate
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class WorkoutRequestRepositoryTest @Autowired constructor(
    val workoutRequestRepository : WorkoutRequestRepository,
    val chatRoomRepository : ChatRoomRepository,
    val memberRepository : MemberRepository,
    val chatParticipantRepository : ChatParticipantRepository,
    val chatMessageRepository : ChatMessageRepository,
    val workoutHistoryRepository: WorkoutHistoryRepository,
    val workoutPartnerRepository : WorkoutPartnerRepository,
    val workoutRequestLogRepository: WorkoutRequestLogRepository,
    val oAuth2Service : OAuth2Service,
    val reviewRepository : ReviewRepository,
    val time : Time,
    val em : EntityManager
) : IntegrationTestSupport() {

    @DisplayName("채팅방의 최근 운동 요청을 조회한다.")
    @Test
    fun findRecentWorkoutRequest() {
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

        val message = ChatMessage(
            member = other,
            chatRoom = chatRoom,
            type = ChatMessageType.WORKOUT_REQUEST,
            sentAt = time.nowLocalDateTime
        )

        val scheduledAt = time.nowLocalDateTime.plusHours(24)
        val requestedAt = time.nowLocalDateTime.minusHours(3)
        val workout = WorkoutRequest(
            chatMessage = message,
            fromMember = other,
            toMember = me,
            status = WorkoutRequestStatus.PENDING,
            location = "location",
            scheduledAt = scheduledAt,
            requestedAt = requestedAt
        )
        chatMessageRepository.save(message)
        workoutRequestRepository.save(workout)

        val chatRoomIds = listOf(chatRoom.id!!)

        //when
        val response = workoutRequestRepository.findRecentWorkoutRequest(chatRoomIds)

        // then
        val status = WorkoutRequestStatusForResponse.from(workout.status!!, requestedAt, scheduledAt, time.nowLocalDateTime)
        assertThat(response[0])
            .extracting("status", "chatRoomId")
            .contains(status, chatRoom.id!!)
    }

    @DisplayName("채팅방의 최근 운동 요청이 완료됐으면 운동 기록 id를 추가로 조회할수있다.")
    @Test
    fun findRecentWorkoutRequestWorkoutHistoryId() {
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

        val message = ChatMessage(
            member = other,
            chatRoom = chatRoom,
            type = ChatMessageType.WORKOUT_REQUEST,
            sentAt = time.nowLocalDateTime
        )

        val scheduledAt = time.nowLocalDateTime.plusHours(24)
        val requestedAt = time.nowLocalDateTime.minusHours(3)
        val workout = WorkoutRequest(
            chatMessage = message,
            fromMember = other,
            toMember = me,
            status = WorkoutRequestStatus.COMPLETE,
            location = "location",
            scheduledAt = scheduledAt,
            requestedAt = requestedAt
        )
        chatMessageRepository.save(message)
        workoutRequestRepository.save(workout)

        val workoutHistory = WorkoutHistory(
            chatRoom = chatRoom,
            workoutRequest = workout,
            memberOne = me,
            memberTwo = other,
            completedAt = time.nowLocalDateTime
        )
        workoutHistoryRepository.save(workoutHistory)

        val chatRoomIds = listOf(chatRoom.id!!)

        //when
        val response = workoutRequestRepository.findRecentWorkoutRequest(chatRoomIds)

        // then
        val status = WorkoutRequestStatusForResponse.from(workout.status!!, requestedAt, scheduledAt, time.nowLocalDateTime)
        assertThat(response[0])
            .extracting("status", "chatRoomId", "workoutHistoryId")
            .contains(status, chatRoom.id!!, workoutHistory.id!!)
    }

    @DisplayName("만료된 운동 요청을 전부 조회한다.")
    @Test
    fun findAllPendingWorkoutRequestAlreadyExpire() {
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
        val chatMessage2 = ChatMessage.ofWorkoutRequest(
            member = me,
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime.minusHours(25)
        )
        val chatMessage3 = ChatMessage.ofWorkoutRequest(
            member = me,
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage1)
        chatMessageRepository.save(chatMessage2)
        chatMessageRepository.save(chatMessage3)

        val workoutRequest1 = WorkoutRequest.of(
            chatMessage = chatMessage1,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        val expireWorkoutRequest1 = WorkoutRequest.of(
            chatMessage = chatMessage2,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime.minusHours(25)
        )
        val expireWorkoutRequest2 = WorkoutRequest.of(
            chatMessage = chatMessage3,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.minusHours(3),
            requestedAt = time.nowLocalDateTime
        )
        workoutRequestRepository.save(workoutRequest1)
        workoutRequestRepository.save(expireWorkoutRequest1)
        workoutRequestRepository.save(expireWorkoutRequest2)


        // when
        val response = workoutRequestRepository.findAllPendingWorkoutRequestAlreadyExpire()

        // then
        assertThat(response)
            .extracting("chatRoomId", "workoutRequestId", "status", "fromMemberId", "toMemberId")
            .containsExactlyInAnyOrder(
                tuple(chatRoom.id!!, expireWorkoutRequest1.id!!, WorkoutRequestStatus.PENDING, me.id!!, other.id!!),
                tuple(chatRoom.id!!, expireWorkoutRequest2.id!!, WorkoutRequestStatus.PENDING, me.id!!, other.id!!),
            )
    }

    @DisplayName("만료 상태의 운동 요청 조회 시, pending 상태가 아닌 운동 요청은 조회하지않는다")
    @Test
    fun findAllExpireWorkoutRequestAlreadyExpire() {
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
        val chatMessage2 = ChatMessage.ofWorkoutRequest(
            member = me,
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime.minusHours(25)
        )
        val chatMessage3 = ChatMessage.ofWorkoutRequest(
            member = me,
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage1)
        chatMessageRepository.save(chatMessage2)
        chatMessageRepository.save(chatMessage3)

        val workoutRequest1 = WorkoutRequest.of(
            chatMessage = chatMessage1,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        val expireWorkoutRequest1 = WorkoutRequest.of(
            chatMessage = chatMessage2,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime.minusHours(25)
        )
        val expireWorkoutRequest2 = WorkoutRequest.of(
            chatMessage = chatMessage3,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.minusHours(3),
            requestedAt = time.nowLocalDateTime
        )
        expireWorkoutRequest2.updateStatus(WorkoutRequestStatus.EXPIRE)
        workoutRequestRepository.save(workoutRequest1)
        workoutRequestRepository.save(expireWorkoutRequest1)
        workoutRequestRepository.save(expireWorkoutRequest2)


        // when
        val response = workoutRequestRepository.findAllPendingWorkoutRequestAlreadyExpire()

        // then
        assertThat(response)
            .extracting("chatRoomId", "workoutRequestId", "status", "fromMemberId", "toMemberId")
            .containsExactlyInAnyOrder(
                tuple(chatRoom.id!!, expireWorkoutRequest1.id!!, WorkoutRequestStatus.PENDING, me.id!!, other.id!!),
            )
    }

    @DisplayName("만료된 운동 요청을 만료 상태로 갱신한다.")
    @Test
    fun updateExpireByIdIn() {
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
        val chatMessage2 = ChatMessage.ofWorkoutRequest(
            member = me,
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime.minusHours(25)
        )
        val chatMessage3 = ChatMessage.ofWorkoutRequest(
            member = me,
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage1)
        chatMessageRepository.save(chatMessage2)
        chatMessageRepository.save(chatMessage3)

        val workoutRequest1 = WorkoutRequest.of(
            chatMessage = chatMessage1,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        val expireWorkoutRequest1 = WorkoutRequest.of(
            chatMessage = chatMessage2,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime.minusHours(25)
        )
        val expireWorkoutRequest2 = WorkoutRequest.of(
            chatMessage = chatMessage3,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.minusHours(3),
            requestedAt = time.nowLocalDateTime
        )
        workoutRequestRepository.save(workoutRequest1)
        workoutRequestRepository.save(expireWorkoutRequest1)
        workoutRequestRepository.save(expireWorkoutRequest2)

        val workoutRequestIds = listOf(expireWorkoutRequest1.id!!, expireWorkoutRequest2.id!!)

        // when
        workoutRequestRepository.updateExpireByIdIn(workoutRequestIds)

        // then
        val workoutRequests = workoutRequestRepository.findAll()
        assertThat(workoutRequests)
            .extracting("id", "status")
            .containsExactlyInAnyOrder(
                tuple(workoutRequest1.id!!, WorkoutRequestStatus.PENDING),
                tuple(expireWorkoutRequest1.id!!, WorkoutRequestStatus.EXPIRE),
                tuple(expireWorkoutRequest2.id!!, WorkoutRequestStatus.EXPIRE),
            )
    }

    @DisplayName("운동 요청과 채팅 메시지를 조회한다.")
    @Test
    fun findByIdAndDeletedAtIsNull() {
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
        val chatMessage2 = ChatMessage.ofWorkoutRequest(
            member = me,
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime.minusHours(25)
        )
        val chatMessage3 = ChatMessage.ofWorkoutRequest(
            member = me,
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage1)
        chatMessageRepository.save(chatMessage2)
        chatMessageRepository.save(chatMessage3)

        val workoutRequest = WorkoutRequest.of(
            chatMessage = chatMessage1,
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
        val findWorkoutRequest = workoutRequestRepository.findWorkoutRequestByIdAndDeletedAtIsNullWithChatMessage(workoutRequest.id!!)

        // then
        assertThat(findWorkoutRequest)
            .extracting("id", "status")
            .contains(workoutRequest.id!!, workoutRequest.status!!)
        assertThat(Hibernate.isInitialized(findWorkoutRequest!!.chatMessage)).isTrue()
    }

    @DisplayName("최근 운동 요청을 조회한다.")
    @Test
    fun findRecentWorkoutRequestEntity() {
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

        val signupRequest = TestDataFactory.oAuth2SignupRequest()
        oAuth2Service.signup(signupRequest, me.id!!)

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
        val chatMessage2 = ChatMessage.ofWorkoutRequest(
            member = me,
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime.plusHours(5)
        )
        chatMessageRepository.save(chatMessage1)
        chatMessageRepository.save(chatMessage2)

        val workoutRequest1 = WorkoutRequest.of(
            chatMessage = chatMessage1,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        val workoutRequest2 = WorkoutRequest.of(
            chatMessage = chatMessage2,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime.plusHours(5)
        )
        workoutRequestRepository.save(workoutRequest1)
        workoutRequestRepository.save(workoutRequest2)


        // when
        val findWorkoutRequest = workoutRequestRepository.findRecentWorkoutRequestEntity(chatRoom.id!!)

        // then
        assertThat(findWorkoutRequest)
            .extracting("id", "status", "scheduledAt")
            .contains(workoutRequest2.id!!, workoutRequest2.status!!, workoutRequest2.scheduledAt)

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
        val response = workoutRequestRepository.findAllWorkoutRequestBy(condition)

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

    @DisplayName("전체 운동 요청을 조회 시, workoutRequestId를 지정하면 그것보다 낮은 id부터 운동 요청을 조회한다.")
    @Test
    fun findAllWorkoutRequestByExistsConditionWorkoutRequestId() {
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
        val log2 = WorkoutRequestLog(
            workoutRequest = workoutRequest2,
            status = WorkoutRequestStatus.PENDING,
            loggedAt = time.nowLocalDateTime.plusHours(5)
        )
        workoutRequestLogRepository.save(log1)
        workoutRequestLogRepository.save(log2)


        val condition = AdminWorkoutRequestCondition(
            workoutRequestId = workoutRequest2.id!!
        )

        // when
        val response = workoutRequestRepository.findAllWorkoutRequestBy(condition)

        // then
        assertThat(response)
            .extracting("workoutRequestId")
            .containsExactly(
                workoutRequest1.id!!
            )
        assertThat(response.hasNext()).isEqualTo(false)
    }


}