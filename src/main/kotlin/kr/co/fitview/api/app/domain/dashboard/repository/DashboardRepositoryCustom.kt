package kr.co.fitview.api.app.domain.dashboard.repository

import kr.co.fitview.api.app.domain.dashboard.dto.response.AdminDashboardToday
import java.time.LocalDate

interface DashboardRepositoryCustom {
    fun findDashboardByDay(targetDate: LocalDate) : AdminDashboardToday?
}