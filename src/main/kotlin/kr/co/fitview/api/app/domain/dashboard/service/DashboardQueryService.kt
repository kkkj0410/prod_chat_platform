package kr.co.fitview.api.app.domain.dashboard.service

import kr.co.fitview.api.app.domain.dashboard.dto.response.AdminDashboardResponse
import kr.co.fitview.api.app.domain.dashboard.dto.response.AdminDashboardToday
import kr.co.fitview.api.app.domain.dashboard.dto.response.AdminDashboardTotal
import kr.co.fitview.api.app.domain.dashboard.repository.DashboardRepository
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.global.GlobalErrorCode
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
        val today = findTodayDashboard()
            ?: throw GlobalException(GlobalErrorCode.ENTITY_NOT_FOUND)

        val total = findTotalDashboard()
            ?: throw GlobalException(GlobalErrorCode.ENTITY_NOT_FOUND)

        return AdminDashboardResponse(
            today = today,
            total = total
        )
    }

    fun findTodayDashboard() : AdminDashboardToday?{
        return dashboardRepository.findDashboardByDay(time.nowLocalDate)
    }

    fun findTotalDashboard() : AdminDashboardTotal?{
        return dashboardRepository.findDashboardByTotal()
    }
}