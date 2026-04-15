package kr.co.fitview.api.app.domain.report.repository

import kr.co.fitview.api.app.domain.report.entity.Report
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface ReportRepository : JpaRepository<Report, Long>, ReportRepositoryCustom {

    fun countByReportedAtBetween(start: LocalDateTime, end: LocalDateTime) : Long

}