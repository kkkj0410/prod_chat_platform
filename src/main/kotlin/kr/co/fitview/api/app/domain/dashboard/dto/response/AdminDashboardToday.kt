package kr.co.fitview.api.app.domain.dashboard.dto.response

data class AdminDashboardToday(
    val memberCount: Long,
    val workoutPartnerCount: Long,
    val workoutHistoryCount: Long,
    val reviewCount: Long,
    val reportCount: Long
)
