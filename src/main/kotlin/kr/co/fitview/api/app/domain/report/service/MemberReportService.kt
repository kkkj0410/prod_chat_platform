package kr.co.fitview.api.app.domain.report.service

import kr.co.fitview.api.app.domain.chat.service.ChatParticipantQueryService
import kr.co.fitview.api.app.domain.chat.service.ChatRoomQueryService
import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.domain.report.dto.request.ReportMemberCreateServiceRequest
import kr.co.fitview.api.app.domain.report.entity.MemberReport
import kr.co.fitview.api.app.domain.report.entity.enums.ReportTargetType
import kr.co.fitview.api.app.domain.report.repository.ChatRoomReportRepository
import kr.co.fitview.api.app.domain.report.repository.MemberReportRepository
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.global.GlobalErrorCode
import kr.co.fitview.api.app.global.exception.error.report.ReportErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class MemberReportService(
    private val memberReportRepository : MemberReportRepository,
    private val reportReasonQueryService: ReportReasonQueryService,
    private val reportService: ReportService,
    private val memberQueryService : MemberQueryService
) {

    @Transactional
    fun addMemberReport(memberId : Long, request : ReportMemberCreateServiceRequest) : MemberReport{

        val findReportReason = reportReasonQueryService.findReportReasonFrom(request.reportReasonId)
            ?: throw GlobalException(GlobalErrorCode.ENTITY_NOT_FOUND)

        if(findReportReason.targetType != ReportTargetType.MEMBER){
            throw GlobalException(ReportErrorCode.INVALID_REPORT_REASON)
        }

        val targetMember = memberQueryService.findMemberFromId(request.memberId)
            ?: throw GlobalException(ReportErrorCode.TARGET_MEMBER_NOT_FOUND)

        val savedReport = reportService.addReport(
            memberId = memberId,
            reportReasonId = findReportReason.id!!,
            targetType = ReportTargetType.MEMBER,
            description = request.description
        )

        val memberReport = MemberReport(
            report = savedReport,
            member = targetMember
        )

        return memberReportRepository.save(memberReport)
    }
}