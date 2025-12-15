package kr.co.fitview.api.app.domain.report.repository

import kr.co.fitview.api.app.domain.report.entity.ChatRoomReport
import org.springframework.data.jpa.repository.JpaRepository

interface ChatRoomReportRepository : JpaRepository<ChatRoomReport, Long> {
}