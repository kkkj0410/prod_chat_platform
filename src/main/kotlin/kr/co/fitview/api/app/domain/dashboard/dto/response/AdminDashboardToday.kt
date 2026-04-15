package kr.co.fitview.api.app.domain.dashboard.dto.response

import com.fasterxml.jackson.annotation.JsonProperty

data class AdminDashboardToday(
    val memberCount: Long,
    val workoutPartnerCount: Long,

    @get:JsonProperty("workoutHistoryCount") // 2026.4.15 - FE에서 사용하는 네이밍은 workoutHistoryCount. 기획에서 운동 요청 개수로 바뀌었기 때문에 내부적으로 workoutRequestCount로 변경
    val workoutRequestCount: Long,

    val reviewCount: Long,
    val reportCount: Long
)
