package kr.co.fitview.api.app.domain.report.repository

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.entity.ChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.chat.repository.ChatMessageRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatParticipantRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatRoomRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.domain.report.condition.AdminReportCondition
import kr.co.fitview.api.app.domain.report.entity.ChatRoomReport
import kr.co.fitview.api.app.domain.report.entity.MemberReport
import kr.co.fitview.api.app.domain.report.entity.Report
import kr.co.fitview.api.app.domain.report.entity.ReportReason
import kr.co.fitview.api.app.domain.report.entity.enums.ReportReasonType
import kr.co.fitview.api.app.domain.report.entity.enums.ReportTargetType
import kr.co.fitview.api.app.domain.review.repository.ReviewRepository
import kr.co.fitview.api.app.domain.review.repository.ReviewTagRelationRepository
import kr.co.fitview.api.app.domain.workout.repository.WorkoutRequestRepository
import kr.co.fitview.api.app.domain.workout_history.repository.WorkoutHistoryRepository
import kr.co.fitview.api.app.domain.workout_partner.repository.WorkoutPartnerRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import kr.co.fitview.api.app.global.util.TestDataFactory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ReportRepositoryTest @Autowired constructor(
    val reportReasonRepository: ReportReasonRepository,
    private val memberRepository : MemberRepository,
    private val chatRoomRepository : ChatRoomRepository,
    private val chatParticipantRepository : ChatParticipantRepository,
    private val reportRepository : ReportRepository,
    private val memberReportRepository : MemberReportRepository,
    private val chatRoomReportRepository : ChatRoomReportRepository,
    private val oAuth2Service : OAuth2Service,
    private val time : Time
) : IntegrationTestSupport() {

    @DisplayName("전체 신고를 조회한다.")
    @Test
    fun findAllReportBy() {
        //given
        val member1 = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        val member2 = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        val member3 = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(member1)
        memberRepository.save(member2)
        memberRepository.save(member3)

        val signupRequest1 = TestDataFactory.oAuth2SignupRequest(
            nickname = "member1"
        )
        oAuth2Service.signup(signupRequest1, member1.id!!)

        val signupRequest2 = TestDataFactory.oAuth2SignupRequest(
            nickname = "member2"
        )
        oAuth2Service.signup(signupRequest2, member2.id!!)

        val signupRequest3 = TestDataFactory.oAuth2SignupRequest(
            nickname = "member3"
        )
        oAuth2Service.signup(signupRequest3, member3.id!!)


        val chatRoom = ChatRoom(ChatRoomType.PRIVATE)
        chatRoomRepository.save(chatRoom)

        val participant1 = ChatParticipant(
            chatRoom = chatRoom,
            member = member1
        )
        val participant2 = ChatParticipant(
            chatRoom = chatRoom,
            member = member3
        )
        chatParticipantRepository.save(participant1)
        chatParticipantRepository.save(participant2)

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
            reportReason = chatReport1,
            targetType = ReportTargetType.CHAT_ROOM,
            reportedAt = time.nowLocalDateTime
        )
        reportRepository.save(report1)
        reportRepository.save(report2)

        val memberChildReport1 = MemberReport(
            report = report1,
            member = member2
        )
        memberReportRepository.save(memberChildReport1)

        val chatChildReport1 = ChatRoomReport(
            report = report2,
            chatRoom = chatRoom
        )
        chatRoomReportRepository.save(chatChildReport1)

        val condition = AdminReportCondition()

        // when
        val response = reportRepository.findAllReportBy(condition)


        // then
        assertThat(response)
            .extracting("reportId", "toMemberNickname", "reportReasonDisplayText", "reportTargetType")
            .containsExactly(
                tuple(
                    report2.id!!,
                    "member1",
                    chatReport1.displayText,
                    ReportTargetType.CHAT_ROOM
                ),
                tuple(
                    report1.id!!,
                    "member2",
                    memberReport1.displayText,
                    ReportTargetType.MEMBER
                )
            )
    }

    @DisplayName("전체 신고를 조회 시, 조건으로 신고 id가 들어오면 해당 신고 id보다 더 낮은 신고 데이터만 가져온다.")
    @Test
    fun findAllReportByExistsConditionReportId() {
        //given
        val member1 = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        val member2 = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        val member3 = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(member1)
        memberRepository.save(member2)
        memberRepository.save(member3)

        val signupRequest1 = TestDataFactory.oAuth2SignupRequest(
            nickname = "member1"
        )
        oAuth2Service.signup(signupRequest1, member1.id!!)

        val signupRequest2 = TestDataFactory.oAuth2SignupRequest(
            nickname = "member2"
        )
        oAuth2Service.signup(signupRequest2, member2.id!!)

        val signupRequest3 = TestDataFactory.oAuth2SignupRequest(
            nickname = "member3"
        )
        oAuth2Service.signup(signupRequest3, member3.id!!)


        val chatRoom = ChatRoom(ChatRoomType.PRIVATE)
        chatRoomRepository.save(chatRoom)

        val participant1 = ChatParticipant(
            chatRoom = chatRoom,
            member = member1
        )
        val participant2 = ChatParticipant(
            chatRoom = chatRoom,
            member = member3
        )
        chatParticipantRepository.save(participant1)
        chatParticipantRepository.save(participant2)

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
            reportReason = chatReport1,
            targetType = ReportTargetType.CHAT_ROOM,
            reportedAt = time.nowLocalDateTime
        )
        reportRepository.save(report1)
        reportRepository.save(report2)

        val memberChildReport1 = MemberReport(
            report = report1,
            member = member2
        )
        memberReportRepository.save(memberChildReport1)

        val chatChildReport1 = ChatRoomReport(
            report = report2,
            chatRoom = chatRoom
        )
        chatRoomReportRepository.save(chatChildReport1)

        val condition = AdminReportCondition(
            reportId = report2.id!!
        )

        // when
        val response = reportRepository.findAllReportBy(condition)


        // then
        assertThat(response.content).hasSize(1)
        assertThat(response.content[0].reportId).isEqualTo(report1.id!!)
        assertThat(response.hasNext()).isEqualTo(false)
    }
}