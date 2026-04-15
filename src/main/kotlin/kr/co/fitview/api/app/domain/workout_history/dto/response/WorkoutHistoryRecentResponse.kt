package kr.co.fitview.api.app.domain.workout_history.dto.response

import java.time.LocalDateTime

data class WorkoutHistoryRecentResponse(
    val workoutHistoryId: Long,
    val nickname: String,
    val completedAt: LocalDateTime,
    val isReviewed: Boolean
)
