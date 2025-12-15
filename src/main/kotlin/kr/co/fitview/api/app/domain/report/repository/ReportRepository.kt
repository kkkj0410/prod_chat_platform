package kr.co.fitview.api.app.domain.report.repository

import kr.co.fitview.api.app.domain.report.entity.Report
import org.springframework.data.jpa.repository.JpaRepository

interface ReportRepository : JpaRepository<Report, Long>