package kr.co.fitview.api.app.domain.dashboard.service

import kr.co.fitview.api.app.domain.dashboard.dto.response.AdminDashboardResponse
import kr.co.fitview.api.app.domain.dashboard.dto.response.AdminDashboardToday
import kr.co.fitview.api.app.domain.dashboard.dto.response.AdminDashboardTotal
import kr.co.fitview.api.app.domain.dashboard.repository.DashboardRepository
import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.domain.report.service.ReportQueryService
import kr.co.fitview.api.app.domain.review.service.ReviewQueryService
import kr.co.fitview.api.app.domain.workout.service.WorkoutRequestQueryService
import kr.co.fitview.api.app.domain.workout_partner.service.WorkoutPartnerQueryService
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.global.GlobalErrorCode
import kr.co.fitview.api.app.global.time.Time
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class DashboardQueryService(
    private val dashboardRepository : DashboardRepository,
    private val memberQueryService: MemberQueryService,
    private val workoutPartnerQueryService: WorkoutPartnerQueryService,
    private val workoutRequestQueryService: WorkoutRequestQueryService,
    private val reviewQueryService : ReviewQueryService,
    private val reportQueryService: ReportQueryService,
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

        val memberCount = memberQueryService.countMemberFromCreatedAtDate(
            date = time.nowLocalDate
        )

        val workoutPartnerCount = workoutPartnerQueryService.countWorkoutPartnerFrom(
            targetDate = time.nowLocalDate,
        )

        val workoutRequestCount = workoutRequestQueryService.countWorkoutRequestFrom(
            targetDate = time.nowLocalDate,
        )

        val reviewCount = reviewQueryService.countReviewFrom(
            targetDate = time.nowLocalDate,
        )

        val reportCount = reportQueryService.countReportFrom(
            targetDate = time.nowLocalDate
        )

        return AdminDashboardToday(
            memberCount = memberCount.toLong(),
            workoutPartnerCount = workoutPartnerCount.toLong(),
            workoutRequestCount = workoutRequestCount.toLong(),
            reviewCount = reviewCount.toLong(),
            reportCount = reportCount.toLong()
        )
    }

    fun findTotalDashboard() : AdminDashboardTotal?{
        return dashboardRepository.findDashboardByTotal()
    }
}