package kr.co.fitview.api.app.domain.report.service

import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.domain.report.condition.AdminReportCondition
import kr.co.fitview.api.app.domain.report.dto.response.AdminReportResponse
import kr.co.fitview.api.app.domain.report.repository.ReportRepository
import kr.co.fitview.api.app.global.time.Time
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalTime


@Service
@Transactional(readOnly = true)
class ReportQueryService(
    private val reportRepository : ReportRepository,
) {

    fun findAllReportFrom(condition: AdminReportCondition) : Slice<AdminReportResponse> {
        return reportRepository.findAllReportBy(condition)
    }

    fun countReportFrom(targetDate : LocalDate) : Int{
        val startOfDay = targetDate.atStartOfDay()
        val endOfDay = targetDate.atTime(LocalTime.MAX)

        return reportRepository.countByReportedAtBetween(startOfDay, endOfDay).toInt()
    }

}