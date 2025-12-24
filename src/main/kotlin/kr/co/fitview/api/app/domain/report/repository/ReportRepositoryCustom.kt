package kr.co.fitview.api.app.domain.report.repository

import kr.co.fitview.api.app.domain.report.condition.AdminReportCondition
import kr.co.fitview.api.app.domain.report.dto.response.AdminReportResponse
import org.springframework.data.domain.Slice

interface ReportRepositoryCustom {

    fun findAllReportBy(condition: AdminReportCondition): Slice<AdminReportResponse>

}