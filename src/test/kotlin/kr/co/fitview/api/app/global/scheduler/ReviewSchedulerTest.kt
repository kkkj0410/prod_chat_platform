package kr.co.fitview.api.app.global.scheduler

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatNoticeMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.chat.repository.ChatMessageRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatNoticeMessageRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatParticipantRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatRoomRepository
import kr.co.fitview.api.app.domain.fcm.dto.request.EventFcmReviewRequest
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.domain.notification.dto.request.EventReviewRequest
import kr.co.fitview.api.app.domain.notification.dto.request.EventWorkoutRequestAccept
import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.domain.review.entity.enums.ReviewReminderLogType
import kr.co.fitview.api.app.domain.review.repository.ReviewReminderLogRepository
import kr.co.fitview.api.app.domain.review.service.ReviewReminderLogService
import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequest
import kr.co.fitview.api.app.domain.workout.repository.WorkoutRequestRepository
import kr.co.fitview.api.app.domain.workout_history.entity.WorkoutHistory
import kr.co.fitview.api.app.domain.workout_history.repository.WorkoutHistoryRepository
import kr.co.fitview.api.app.domain.workout_history.service.WorkoutHistoryQueryService
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import kr.co.fitview.api.app.global.util.TestDataFactory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher

class ReviewSchedulerTest @Autowired constructor(
    private val reviewReminderLogRepository: ReviewReminderLogRepository,
    private val workoutHistoryRepository: WorkoutHistoryRepository,
    private val memberRepository : MemberRepository,
    private val chatRoomRepository : ChatRoomRepository,
    private val chatMessageRepository : ChatMessageRepository,
    private val chatNoticeMessageRepository: ChatNoticeMessageRepository,
    private val workoutRequestRepository: WorkoutRequestRepository,
    private val oAuth2Service : OAuth2Service,
    private val reviewScheduler : ReviewScheduler,
    private val chatParticipantRepository: ChatParticipantRepository,
    private val time: Time,

    ) : IntegrationTestSupport() {

    @DisplayName("운동 이력이 24시간이 지난 경우, 리뷰 이력이 없으면 리뷰 알림 이력을 저장한다.")
    @Test
    fun sendReviewReminderForHistoriesExceeded24h() {
        // given
        val me = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        val other = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(me)
        memberRepository.save(other)

        val signupRequest1 = TestDataFactory.oAuth2SignupRequest()
        oAuth2Service.signup(signupRequest1, me.id!!)

        val signupRequest2 = TestDataFactory.oAuth2SignupRequest()
        oAuth2Service.signup(signupRequest2, other.id!!)

        val chatRoom = ChatRoom(ChatRoomType.PRIVATE)
        chatRoomRepository.save(chatRoom)

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

        val chatMessage = ChatMessage(
            member = me,
            chatRoom = chatRoom,
            type = ChatMessageType.WORKOUT_REQUEST,
            content = "content",
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage)

        val chatMessage2 = ChatMessage(
            member = me,
            chatRoom = chatRoom,
            type = ChatMessageType.WORKOUT_REQUEST,
            content = "content",
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage2)

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
            requestedAt = time.nowLocalDateTime.minusDays(1)
        )
        workoutRequestRepository.save(workoutRequest2)

        val workoutHistory = WorkoutHistory(
            chatRoom = chatRoom,
            workoutRequest = workoutRequest,
            memberOne = me,
            memberTwo = other,
            completedAt = time.nowLocalDateTime.minusHours(24)
        )
        workoutHistoryRepository.save(workoutHistory)

        val workoutHistory2 = WorkoutHistory(
            chatRoom = chatRoom,
            workoutRequest = workoutRequest2,
            memberOne = me,
            memberTwo = other,
            completedAt = time.nowLocalDateTime.minusHours(23).minusMinutes(59)
        )
        workoutHistoryRepository.save(workoutHistory2)

        // when
        reviewScheduler.sendReviewReminderForHistoriesExceeded24h()

        // then
        val reviewReminderLogs = reviewReminderLogRepository.findAll()

        assertThat(reviewReminderLogs).hasSize(1)
        assertThat(reviewReminderLogs)
            .extracting("workoutHistory", "type", "sentAt")
            .contains(
                tuple(workoutHistory, ReviewReminderLogType.REVIEW_24H, time.nowLocalDateTime),
            )
    }

    @DisplayName("운동 이력이 24시간이 지난 경우, 리뷰 권유 푸시 알람을 양측에 모두 보낸다")
    @Test
    fun sendReviewReminderForHistoriesExceeded24hFcm() {
        // given
        val me = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        val other = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(me)
        memberRepository.save(other)

        val signupRequest1 = TestDataFactory.oAuth2SignupRequest()
        oAuth2Service.signup(signupRequest1, me.id!!)

        val signupRequest2 = TestDataFactory.oAuth2SignupRequest()
        oAuth2Service.signup(signupRequest2, other.id!!)

        val chatRoom = ChatRoom(ChatRoomType.PRIVATE)
        chatRoomRepository.save(chatRoom)

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

        val chatMessage = ChatMessage(
            member = me,
            chatRoom = chatRoom,
            type = ChatMessageType.WORKOUT_REQUEST,
            content = "content",
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage)

        val chatMessage2 = ChatMessage(
            member = me,
            chatRoom = chatRoom,
            type = ChatMessageType.WORKOUT_REQUEST,
            content = "content",
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage2)

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
            requestedAt = time.nowLocalDateTime.minusDays(1)
        )
        workoutRequestRepository.save(workoutRequest2)

        val workoutHistory = WorkoutHistory(
            chatRoom = chatRoom,
            workoutRequest = workoutRequest,
            memberOne = me,
            memberTwo = other,
            completedAt = time.nowLocalDateTime.minusHours(24)
        )
        workoutHistoryRepository.save(workoutHistory)

        val workoutHistory2 = WorkoutHistory(
            chatRoom = chatRoom,
            workoutRequest = workoutRequest2,
            memberOne = me,
            memberTwo = other,
            completedAt = time.nowLocalDateTime.minusHours(24)
        )
        workoutHistoryRepository.save(workoutHistory2)

        val chatMessageFromNotice1 = ChatMessage.ofNotice(
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessageFromNotice1)

        val chatNoticeMessage1 = ChatNoticeMessage.ofWorkoutHistory(
            chatMessage = chatMessageFromNotice1,
            workoutHistory = workoutHistory
        )
        chatNoticeMessageRepository.save(chatNoticeMessage1)

        val chatMessageFromNotice2 = ChatMessage.ofNotice(
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessageFromNotice2)

        val chatNoticeMessage2 = ChatNoticeMessage.ofWorkoutHistory(
            chatMessage = chatMessageFromNotice2,
            workoutHistory = workoutHistory2
        )
        chatNoticeMessageRepository.save(chatNoticeMessage2)

        // when
        reviewScheduler.sendReviewReminderForHistoriesExceeded24h()

        // then
        val count = events.stream(EventFcmReviewRequest::class.java).count()
        assertThat(count).isEqualTo(4)
    }

    @DisplayName("운동 이력이 24시간이 지난 경우, 리뷰 권유 인앱 알람을 양측에 모두 보낸다")
    @Test
    fun sendReviewReminderForHistoriesExceeded24hNotification() {
        // given
        val me = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        val other = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(me)
        memberRepository.save(other)

        val signupRequest1 = TestDataFactory.oAuth2SignupRequest()
        oAuth2Service.signup(signupRequest1, me.id!!)

        val signupRequest2 = TestDataFactory.oAuth2SignupRequest()
        oAuth2Service.signup(signupRequest2, other.id!!)

        val chatRoom = ChatRoom(ChatRoomType.PRIVATE)
        chatRoomRepository.save(chatRoom)

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

        val chatMessage = ChatMessage(
            member = me,
            chatRoom = chatRoom,
            type = ChatMessageType.WORKOUT_REQUEST,
            content = "content",
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage)

        val chatMessage2 = ChatMessage(
            member = me,
            chatRoom = chatRoom,
            type = ChatMessageType.WORKOUT_REQUEST,
            content = "content",
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage2)

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
            requestedAt = time.nowLocalDateTime.minusDays(1)
        )
        workoutRequestRepository.save(workoutRequest2)

        val workoutHistory = WorkoutHistory(
            chatRoom = chatRoom,
            workoutRequest = workoutRequest,
            memberOne = me,
            memberTwo = other,
            completedAt = time.nowLocalDateTime.minusHours(24)
        )
        workoutHistoryRepository.save(workoutHistory)

        val workoutHistory2 = WorkoutHistory(
            chatRoom = chatRoom,
            workoutRequest = workoutRequest2,
            memberOne = me,
            memberTwo = other,
            completedAt = time.nowLocalDateTime.minusHours(24)
        )
        workoutHistoryRepository.save(workoutHistory2)

        val chatMessageFromNotice1 = ChatMessage.ofNotice(
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessageFromNotice1)

        val chatNoticeMessage1 = ChatNoticeMessage.ofWorkoutHistory(
            chatMessage = chatMessageFromNotice1,
            workoutHistory = workoutHistory
        )
        chatNoticeMessageRepository.save(chatNoticeMessage1)

        val chatMessageFromNotice2 = ChatMessage.ofNotice(
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessageFromNotice2)

        val chatNoticeMessage2 = ChatNoticeMessage.ofWorkoutHistory(
            chatMessage = chatMessageFromNotice2,
            workoutHistory = workoutHistory2
        )
        chatNoticeMessageRepository.save(chatNoticeMessage2)

        // when
        reviewScheduler.sendReviewReminderForHistoriesExceeded24h()

        // then
        val count = events.stream(EventReviewRequest::class.java).count()
        assertThat(count).isEqualTo(4)
    }
}