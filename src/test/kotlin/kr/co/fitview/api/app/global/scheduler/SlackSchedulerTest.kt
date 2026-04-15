package kr.co.fitview.api.app.global.scheduler

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.chat.repository.ChatMessageRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatParticipantRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatRoomRepository
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
import kr.co.fitview.api.app.domain.stat.entity.ActiveMemberStat
import kr.co.fitview.api.app.domain.stat.entity.ApiStat
import kr.co.fitview.api.app.domain.stat.entity.enums.ApiStatMethod
import kr.co.fitview.api.app.domain.stat.enums.ApiStatPathMeta
import kr.co.fitview.api.app.domain.stat.repository.ActiveMemberStatRepository
import kr.co.fitview.api.app.domain.stat.repository.ApiStatRepository
import kr.co.fitview.api.app.domain.stat.service.ApiStatQueryService
import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequest
import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import kr.co.fitview.api.app.domain.workout.repository.WorkoutRequestRepository
import kr.co.fitview.api.app.domain.workout_history.entity.WorkoutHistory
import kr.co.fitview.api.app.domain.workout_history.repository.WorkoutHistoryRepository
import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartner
import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartnerRequest
import kr.co.fitview.api.app.domain.workout_partner.entity.enums.WorkoutPartnerRequestContent
import kr.co.fitview.api.app.domain.workout_partner.entity.enums.WorkoutPartnerRequestStatus
import kr.co.fitview.api.app.domain.workout_partner.repository.WorkoutPartnerRepository
import kr.co.fitview.api.app.domain.workout_partner.repository.WorkoutPartnerRequestRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.times
import org.mockito.kotlin.then
import org.springframework.beans.factory.annotation.Autowired
import org.mockito.kotlin.argThat
import org.mockito.kotlin.argumentCaptor

class SlackSchedulerTest @Autowired constructor(
    private val slackScheduler : SlackScheduler,
    private val apiStatRepository: ApiStatRepository,
    private val activeMemberStatRepository: ActiveMemberStatRepository,
    private val memberRepository : MemberRepository,
    private val time : Time,
    private val workoutPartnerRepository : WorkoutPartnerRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val chatParticipantRepository: ChatParticipantRepository,
    private val chatRoomRepository: ChatRoomRepository,
    private val workoutRequestRepository: WorkoutRequestRepository,
    private val workoutHistoryRepository : WorkoutHistoryRepository,
    private val workoutPartnerRequestRepository: WorkoutPartnerRequestRepository,
    private val reviewRepository : ReviewRepository,
    private val reportReasonRepository : ReportReasonRepository,
    private val reportRepository : ReportRepository,
) : IntegrationTestSupport(){


    @DisplayName("슬랙 채널에 정해진 포맷의 어제 통계값을 보낸다.")
    @Test
    fun sendSlackFormat() {
        // given
        val yesterday = time.nowLocalDate.minusDays(1)

        val member1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
            isSignup = true,
            signupAt = time.nowLocalDateTime.minusDays(2)
        )
        member1.createdAt = time.nowLocalDateTime.minusDays(2)

        val member2 = Member(
            email = "email2",
            password = "password2",
            role = Role.USER,
            isSignup = true,
            signupAt = time.nowLocalDateTime.minusDays(2)
        )
        member2.createdAt = time.nowLocalDateTime.minusDays(2)

        val notSignupMember = Member(
            email = "email3",
            password = "password3",
            role = Role.USER,
            isSignup = false,
        )
        notSignupMember.createdAt = time.nowLocalDateTime.minusDays(1)

        memberRepository.save(member1)
        memberRepository.save(member2)
        memberRepository.save(notSignupMember)

        val activeMemberStat1 = ActiveMemberStat.of(
            member = member1,
            statDate = yesterday
        )
        val activeMemberStat2 = ActiveMemberStat.of(
            member = member2,
            statDate = yesterday
        )
        activeMemberStatRepository.save(activeMemberStat1)
        activeMemberStatRepository.save(activeMemberStat2)

        val workoutPartnerRequest = WorkoutPartnerRequest(
            fromMember = member1,
            toMember = member2,
            status = WorkoutPartnerRequestStatus.PENDING,
            requestedAt = time.nowLocalDateTime.minusDays(1),
            content = WorkoutPartnerRequestContent.BURN
        )
        workoutPartnerRequestRepository.save(workoutPartnerRequest)

        val workoutPartner = WorkoutPartner.of(
            memberOne = member1,
            memberTwo = member2
        )
        workoutPartner.createdAt = time.nowLocalDateTime.minusDays(1)
        workoutPartnerRepository.save(workoutPartner)

        val chatRoom = chatRoomRepository.save(ChatRoom(ChatRoomType.PRIVATE))
        chatParticipantRepository.save(ChatParticipant(chatRoom, member1))
        chatParticipantRepository.save(ChatParticipant(chatRoom, member2))

        val chatMessage = ChatMessage(
            member = member1,
            chatRoom = chatRoom,
            type = ChatMessageType.WORKOUT_REQUEST,
            sentAt = time.nowLocalDateTime.minusDays(1)
        )
        chatMessageRepository.save(chatMessage)

        val workoutRequest = WorkoutRequest(
            chatMessage = chatMessage,
            fromMember = member1,
            toMember = member2,
            status = WorkoutRequestStatus.PENDING,
            location = "헬스장",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime.minusDays(1)
        )
        workoutRequestRepository.save(workoutRequest)

        val workoutHistory = WorkoutHistory.of(
            chatRoom = chatRoom,
            workoutRequest = workoutRequest,
            memberOne = member1,
            memberTwo = member2,
            completedAt = time.nowLocalDateTime.minusDays(1)
        )
        workoutHistoryRepository.save(workoutHistory)

        val review = Review(
            fromMember = member1,
            toMember = member2,
            workoutHistory = workoutHistory,
            isPrivate = false,
            type = ReviewType.GOOD,
            score = 2.0,
            content = "좋아요",
            postedAt = time.nowLocalDateTime.minusDays(1)
        )
        reviewRepository.save(review)

        val reportReason = ReportReason(
            targetType = ReportTargetType.MEMBER,
            reasonType = ReportReasonType.OTHER,
            displayText = "기타",
            seq = 100
        )
        reportReasonRepository.save(reportReason)

        val report = Report(
            member = member1,
            reportReason = reportReason,
            targetType = ReportTargetType.MEMBER,
            reportedAt = time.nowLocalDateTime.minusDays(1)
        )
        reportRepository.save(report)

        val apiStat1 = ApiStat(
            statDate = yesterday,
            method = ApiStatMethod.GET,
            path = "/api/v1/hello1/{id}",
            count = 100L
        )
        val apiStat2 = ApiStat(
            statDate = yesterday,
            method = ApiStatMethod.GET,
            path = "/api/v1/hello2/{id}",
            count = 200L
        )
        val apiStat3 = ApiStat(
            statDate = yesterday,
            method = ApiStatPathMeta.MEMBERS_LOCAL.method,
            path = ApiStatPathMeta.MEMBERS_LOCAL.path,
            count = 300L
        )
        apiStatRepository.save(apiStat1)
        apiStatRepository.save(apiStat2)
        apiStatRepository.save(apiStat3)

        //when
        slackScheduler.sendSlack()

        //then
        val captor = argumentCaptor<String>()
        then(slackNotifier).should(times(1)).send(captor.capture())
        val actual = captor.firstValue

        val startWauDate = yesterday.minusDays(6)
        val startMonthDate = yesterday.withDayOfMonth(1)

        val expected = """
📝 FITVIEW 일일 리포트 (${time.nowLocalDate})

📊 활성 사용자
- DAU ($yesterday): 2명
- WAU ($startWauDate ~ $yesterday): 2명
- MAU ($startMonthDate ~ $yesterday): 2명

📈 리텐션 (재방문율)
- D1 리텐션 (${yesterday.minusDays(1)} 가입자 대상): 100% (2명 중 2명 재방문)
- D7 리텐션 (${yesterday.minusDays(7)} 가입자 대상): 0% (0명 중 0명 재방문)

📈 사용자 행동
- 핏버디 요청건수: 1건
- 핏버디 매칭건수: 1건
- 운동 약속건수: 1건
- 후기 작성건수: 1건
- 신고 접수건수: 1건

📥 유입 및 전환
- 계정 생성 ($yesterday): 1명
- 회원가입 완료: 0명
- 회원가입 미완료: 1명

🔝 Top 5 API 호출 ($yesterday)
1. 우리 동네 핏버디 조회 - 300회
2. 설명 없음 - 200회
3. 설명 없음 - 100회
""".trimIndent()

        assertThat(actual).isEqualTo(expected)
    }
}