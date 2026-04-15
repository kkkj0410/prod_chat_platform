package kr.co.fitview.api.app.domain.dashboard.repository

import kr.co.fitview.api.app.domain.dashboard.dto.response.AdminDashboardToday
import kr.co.fitview.api.app.domain.dashboard.dto.response.AdminDashboardTotal
import java.time.LocalDate

interface DashboardRepositoryCustom {
    fun findDashboardByTotal(): AdminDashboardTotal?
}