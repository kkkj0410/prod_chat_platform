package kr.co.fitview.api.app.domain.report.service

import kr.co.fitview.api.app.domain.report.dto.response.ReportReasonResponse
import kr.co.fitview.api.app.domain.report.entity.enums.ReportTargetType
import kr.co.fitview.api.app.domain.report.repository.ReportReasonRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class ReportReasonQueryService(
    private val reportReasonRepository: ReportReasonRepository
) {

    fun findAllReportReason(type: ReportTargetType): List<ReportReasonResponse> {
        val findReportReasons = reportReasonRepository.findAllByTargetTypeAndDeletedAtIsNullOrderBySeqAsc(type)

        val response = findReportReasons.map {
            ReportReasonResponse(
                reportReasonId = it.id!!,
                displayText = it.displayText!!
            )
        }
        return response
    }
}