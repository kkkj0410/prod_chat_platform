package kr.co.fitview.api.app.domain.dashboard.service

import jakarta.persistence.EntityManager
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
import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartner
import kr.co.fitview.api.app.domain.workout_partner.repository.WorkoutPartnerRepository
import kr.co.fitview.api.app.global.entity.OAuth2Provider
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

import org.springframework.beans.factory.annotation.Autowired

class DashboardRepositoryTest @Autowired constructor(
    val workoutPartnerRepository : WorkoutPartnerRepository,
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
    val time : Time,
    val em : EntityManager
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

        val workoutPartner = WorkoutPartner.of(
            memberOne = member1,
            memberTwo = member3
        )
        workoutPartnerRepository.save(workoutPartner)


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
        val response = dashboardRepository.findDashboardByDay(time.nowLocalDate)

        // then
        assertThat(response)
            .extracting("memberCount", "workoutPartnerCount", "workoutHistoryCount", "reviewCount", "reportCount")
            .contains(
                3L, 1L, 1L, 2L, 2L
            )
    }

    @DisplayName("특정날의 사용자 현황 조회 시, 특정날에 속하지 않은 회원은 무시한다.")
    @Test
    fun findDashboardByDayNotTargetTime() {
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

        val notTimeMember = Member(
            email = "email3",
            password = "password3",
            role = Role.USER,
        )
        memberRepository.save(notTimeMember)

        em.createQuery(
            "update Member m set m.createdAt = :time where m.id = :id"
        )
            .setParameter("time", time.nowLocalDateTime.plusDays(1).plusSeconds(1))
            .setParameter("id", notTimeMember.id)
            .executeUpdate()

        // when
        val response = dashboardRepository.findDashboardByDay(time.nowLocalDate)

        // then
        assertThat(response)
            .extracting("memberCount", "workoutPartnerCount", "workoutHistoryCount", "reviewCount", "reportCount")
            .contains(
                3L, 0L, 0L, 0L, 0L
            )
    }

    @DisplayName("특정날의 사용자 현황 조회 시, 어드민 계정은 조회대상에서 제외한다.")
    @Test
    fun findDashboardByDayNotAdmin() {
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

        val notUserMember = Member(
            email = "email3",
            password = "password3",
            role = Role.ADMIN,
        )
        memberRepository.save(notUserMember)

        // when
        val response = dashboardRepository.findDashboardByDay(time.nowLocalDate)

        // then
        assertThat(response)
            .extracting("memberCount", "workoutPartnerCount", "workoutHistoryCount", "reviewCount", "reportCount")
            .contains(
                3L, 0L, 0L, 0L, 0L
            )

    }

    @DisplayName("전체 사용자 현황을 조회한다.")
    @Test
    fun findDashboardByTotal() {
        // given
        val member1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
            provider = OAuth2Provider.APPLE
        )
        val member2 = Member(
            email = "email2",
            password = "password2",
            role = Role.USER,
            provider = OAuth2Provider.APPLE
        )
        val member3 = Member(
            email = "email2",
            password = "password2",
            role = Role.USER,
            provider = OAuth2Provider.APPLE
        )
        memberRepository.save(member1)
        memberRepository.save(member2)
        memberRepository.save(member3)

        val workoutPartner = WorkoutPartner.of(
            memberOne = member1,
            memberTwo = member3
        )
        workoutPartnerRepository.save(workoutPartner)


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

        // when
        val response = dashboardRepository.findDashboardByTotal()

        // then
        assertThat(response)
            .extracting("memberCount", "workoutPartnerCount", "workoutHistoryCount", "reviewCount")
            .contains(
                3L, 1L, 1L, 2L
            )
    }

    @DisplayName("전체 사용자 현황 조회 시, 오늘 생성된 데이터가 아니더라도 조회대상에 모두 포함된다.")
    @Test
    fun findDashboardByTotalNotToday() {
        // given
        val member1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
            provider = OAuth2Provider.APPLE
        )
        val member2 = Member(
            email = "email2",
            password = "password2",
            role = Role.USER,
            provider = OAuth2Provider.APPLE
        )
        val member3 = Member(
            email = "email2",
            password = "password2",
            role = Role.USER,
            provider = OAuth2Provider.APPLE
        )
        memberRepository.save(member1)
        memberRepository.save(member2)
        memberRepository.save(member3)

        val notTimeMember = Member(
            email = "email3",
            password = "password3",
            role = Role.USER,
            provider = OAuth2Provider.APPLE
        )
        memberRepository.save(notTimeMember)

        em.createQuery(
            "update Member m set m.createdAt = :time where m.id = :id"
        )
            .setParameter("time", time.nowLocalDateTime.plusDays(1).plusSeconds(1))
            .setParameter("id", notTimeMember.id)
            .executeUpdate()

        // when
        val response = dashboardRepository.findDashboardByTotal()

        // then
        assertThat(response)
            .extracting("memberCount", "workoutPartnerCount", "workoutHistoryCount", "reviewCount")
            .contains(
                4L, 0L, 0L, 0L
            )
    }

    @DisplayName("전체 사용자 현황 조회 시, 어드민은 제외한다.")
    @Test
    fun findDashboardByTotalNotAdmin() {
        // given
        val member1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
            provider = OAuth2Provider.APPLE
        )
        val member2 = Member(
            email = "email2",
            password = "password2",
            role = Role.USER,
            provider = OAuth2Provider.APPLE
        )
        val member3 = Member(
            email = "email2",
            password = "password2",
            role = Role.USER,
            provider = OAuth2Provider.APPLE
        )
        memberRepository.save(member1)
        memberRepository.save(member2)
        memberRepository.save(member3)

        val notUserMember = Member(
            email = "email3",
            password = "password3",
            role = Role.ADMIN,
        )
        memberRepository.save(notUserMember)

        // when
        val response = dashboardRepository.findDashboardByTotal()

        // then
        assertThat(response)
            .extracting("memberCount", "workoutPartnerCount", "workoutHistoryCount", "reviewCount")
            .contains(
                3L, 0L, 0L, 0L
            )
    }

    @DisplayName("전체 사용자 현황 조회 시, 로컬 로그인 회원은 제외한다..")
    @Test
    fun findDashboardByTotalNotLocal() {
        // given
        val member1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
            provider = OAuth2Provider.APPLE
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

        val notUserMember = Member(
            email = "email3",
            password = "password3",
            role = Role.ADMIN,
        )
        memberRepository.save(notUserMember)

        // when
        val response = dashboardRepository.findDashboardByTotal()

        // then
        assertThat(response)
            .extracting("memberCount", "workoutPartnerCount", "workoutHistoryCount", "reviewCount")
            .contains(
                1L, 0L, 0L, 0L
            )
    }

}