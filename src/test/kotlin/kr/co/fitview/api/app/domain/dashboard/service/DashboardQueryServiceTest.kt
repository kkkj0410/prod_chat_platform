package kr.co.fitview.api.app.domain.dashboard.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.chat.repository.*
import kr.co.fitview.api.app.domain.dashboard.repository.DashboardRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.domain.report.entity.Report
import kr.co.fitview.api.app.domain.report.entity.ReportReason
import kr.co.fitview.api.app.domain.report.entity.enums.ReportReasonType
import kr.co.fitview.api.app.domain.report.entity.enums.ReportTargetType
import kr.co.fitview.api.app.domain.report.repository.ReportReasonRepository
import kr.co.fitview.api.app.domain.report.repository.ReportRepository
import kr.co.fitview.api.app.domain.review.entity.Review
import kr.co.fitview.api.app.domain.review.entity.enums.ReviewType
import kr.co.fitview.api.app.domain.review.repository.ReviewRepository
import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequest
import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import kr.co.fitview.api.app.domain.workout.repository.WorkoutRequestRepository
import kr.co.fitview.api.app.domain.workout_history.entity.WorkoutHistory
import kr.co.fitview.api.app.domain.workout_history.repository.WorkoutHistoryRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

import org.springframework.beans.factory.annotation.Autowired

class DashboardQueryServiceTest @Autowired constructor(
    val chatMessageRepository: ChatMessageRepository,
    val chatParticipantRepository: ChatParticipantRepository,
    val chatRoomRepository: ChatRoomRepository,
    val workoutRequestRepository: WorkoutRequestRepository,
    val workoutHistoryRepository : WorkoutHistoryRepository,
    val oAuth2Service: OAuth2Service,
    val memberRepository: MemberRepository,
    val reviewRepository : ReviewRepository,
    val reportReasonRepository : ReportReasonRepository,
    val reportRepository : ReportRepository,
    val dashboardRepository: DashboardRepository,
    val time : Time
) : IntegrationTestSupport() {

    @DisplayName("특정날의 사용자 현황을 조회한다.")
    @Test
    fun findDashboardByDay() {
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
        val member3 = Member(
            email = "email2",
            password = "password2",
            role = Role.USER,
        )
        memberRepository.save(member1)
        memberRepository.save(member2)
        memberRepository.save(member3)

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


        val message1ByChatRoom2 = ChatMessage(
            member = member3,
            chatRoom = chatRoom2,
            type = ChatMessageType.WORKOUT_REQUEST,
            sentAt = time.nowLocalDateTime.minusHours(3)
        )
        val workout1ByChatRoom2 = WorkoutRequest(
            chatMessage = message1ByChatRoom2,
            fromMember = member3,
            toMember = member1,
            status = WorkoutRequestStatus.PENDING,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusHours(24),
            requestedAt = time.nowLocalDateTime.minusHours(3)
        )
        chatRoom2.updateLastMessageAt(time.nowLocalDateTime.minusHours(3))

        chatMessageRepository.save(message1ByChatRoom2)

        workoutRequestRepository.save(workout1ByChatRoom2)

        val workoutHistory = WorkoutHistory.of(
            chatRoom = chatRoom2,
            workoutRequest = workout1ByChatRoom2,
            memberOne = member1,
            memberTwo = member3,
            completedAt = time.nowLocalDateTime
        )
        workoutHistoryRepository.save(workoutHistory)

        val review1 = Review(
            fromMember = member1,
            toMember = member3,
            workoutHistory = workoutHistory,
            isPrivate = false,
            type = ReviewType.GOOD,
            score = 2.0,
            content = "content",
            postedAt = time.nowLocalDateTime
        )
        val review2 = Review(
            fromMember = member3,
            toMember = member1,
            workoutHistory = workoutHistory,
            isPrivate = false,
            type = ReviewType.GOOD,
            score = 2.0,
            content = "content",
            postedAt = time.nowLocalDateTime
        )
        reviewRepository.save(review1)
        reviewRepository.save(review2)

        val memberReport1 = ReportReason(
            targetType = ReportTargetType.MEMBER,
            reasonType = ReportReasonType.OTHER,
            displayText = "memberDisplay1",
            seq = 100
        )
        val chatReport1 = ReportReason(
            targetType = ReportTargetType.CHAT_ROOM,
            reasonType = ReportReasonType.INAPPROPRIATE_REQUEST,
            displayText = "chatDisplay1",
            seq = 100
        )

        reportReasonRepository.save(memberReport1)
        reportReasonRepository.save(chatReport1)

        val report1 = Report(
            member = member1,
            reportReason = memberReport1,
            targetType = ReportTargetType.MEMBER,
            reportedAt = time.nowLocalDateTime
        )
        val report2 = Report(
            member = member3,
            reportReason = memberReport1,
            targetType = ReportTargetType.CHAT_ROOM,
            reportedAt = time.nowLocalDateTime
        )
        reportRepository.save(report1)
        reportRepository.save(report2)

        // when
        dashboardRepository.findDashboardByDay(time.nowLocalDate)


        // then

    }

}