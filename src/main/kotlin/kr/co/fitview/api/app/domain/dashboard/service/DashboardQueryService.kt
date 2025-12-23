package kr.co.fitview.api.app.domain.dashboard.service

import kr.co.fitview.api.app.domain.dashboard.dto.response.AdminDashboardResponse
import kr.co.fitview.api.app.domain.dashboard.dto.response.AdminDashboardToday
import kr.co.fitview.api.app.domain.dashboard.dto.response.AdminDashboardTotal
import kr.co.fitview.api.app.domain.dashboard.repository.DashboardRepository
import kr.co.fitview.api.app.global.time.Time
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class DashboardQueryService(
    private val dashboardRepository : DashboardRepository,
    private val time : Time
) {

    fun findDashboardComposite() : AdminDashboardResponse {
        TODO()
    }

    fun findTodayDashboard() : AdminDashboardToday{
        dashboardRepository.findDashboardByDay(time.nowLocalDate)

        TODO()
    }

    fun findTotalDashboard() : AdminDashboardTotal{
        TODO()
    }
}