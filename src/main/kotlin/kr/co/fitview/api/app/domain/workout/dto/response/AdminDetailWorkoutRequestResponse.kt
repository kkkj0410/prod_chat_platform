package kr.co.fitview.api.app.domain.workout.dto.response

import java.time.LocalDateTime

data class AdminDetailWorkoutRequestResponse(

    val workoutPartnerId : Long,
    val workoutRequestId : Long,

    val scheduledAt : LocalDateTime,
    val location : String,

    val workoutRequestLogs : List<AdminDetailWorkoutRequestLogResponse>,
    val reviews : List<AdminDetailReviewResponse>
)
