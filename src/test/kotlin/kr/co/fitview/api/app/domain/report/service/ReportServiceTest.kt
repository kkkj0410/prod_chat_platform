package kr.co.fitview.api.app.domain.report.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.repository.ChatParticipantRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatRoomRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.report.entity.ReportReason
import kr.co.fitview.api.app.domain.report.entity.enums.ReportReasonType
import kr.co.fitview.api.app.domain.report.entity.enums.ReportTargetType
import kr.co.fitview.api.app.domain.report.repository.ReportReasonRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ReportServiceTest @Autowired constructor(
    val reportReasonRepository: ReportReasonRepository,
    val memberRepository : MemberRepository,
    val chatRoomRepository : ChatRoomRepository,
    val chatParticipantRepository : ChatParticipantRepository,
    val reportService : ReportService,
    val time : Time
) : IntegrationTestSupport(){

    @DisplayName("신고를 생성한다.")
    @Test
    fun addReport() {
        // given
        val reportReason = ReportReason(
            targetType = ReportTargetType.CHAT_ROOM,
            reasonType = ReportReasonType.INAPPROPRIATE_REQUEST,
            displayText = "chatDisplay1",
            seq = 100
        )
        reportReasonRepository.save(reportReason)

        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(member)

        // when
        val savedReport = reportService.addReport(
            memberId = member.id!!,
            reportReasonId = reportReason.id!!,
            targetType = ReportTargetType.CHAT_ROOM,
            description = null
        )

        // then
        assertThat(savedReport)
            .extracting("member", "reportReason", "targetType", "description", "reportedAt")
            .contains(member, reportReason, ReportTargetType.CHAT_ROOM, null, time.nowLocalDateTime)
    }
}