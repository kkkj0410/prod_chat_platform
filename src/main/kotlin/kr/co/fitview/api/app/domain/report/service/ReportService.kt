package kr.co.fitview.api.app.domain.report.service

import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.domain.report.entity.Report
import kr.co.fitview.api.app.domain.report.entity.enums.ReportTargetType
import kr.co.fitview.api.app.domain.report.repository.ReportRepository
import kr.co.fitview.api.app.global.time.Time
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class ReportService(
    private val memberQueryService: MemberQueryService,
    private val reportReasonQueryService : ReportReasonQueryService,
    private val reportRepository : ReportRepository,
    private val time : Time
) {
    @Transactional
    fun addReport(memberId : Long, reportReasonId : Long, targetType : ReportTargetType, description : String?) : Report{

        val findMember = memberQueryService.findMemberReferenceFrom(memberId)
        val findReportReason = reportReasonQueryService.findReportReasonReferenceFrom(reportReasonId)

        val report = Report(
            member = findMember,
            reportReason = findReportReason,
            targetType = targetType,
            description = description,
            reportedAt = time.nowLocalDateTime
        )

        return reportRepository.save(report)
    }
}