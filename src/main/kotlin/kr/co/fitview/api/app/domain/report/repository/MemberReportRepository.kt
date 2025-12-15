package kr.co.fitview.api.app.domain.report.repository

import kr.co.fitview.api.app.domain.report.entity.ChatRoomReport
import kr.co.fitview.api.app.domain.report.entity.MemberReport
import org.springframework.data.jpa.repository.JpaRepository

interface MemberReportRepository : JpaRepository<MemberReport, Long> {
}