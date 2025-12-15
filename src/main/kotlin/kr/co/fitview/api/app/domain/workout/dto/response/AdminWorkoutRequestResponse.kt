package kr.co.fitview.api.app.domain.workout.dto.response

import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import java.time.LocalDateTime

data class AdminWorkoutRequestResponse(

    val workoutPartnerId : Long,
    val workoutRequestId : Long,
    val fromMemberNickname : String,
    val toMemberNickname : String,
    val workoutRequestStatus : WorkoutRequestStatus,
    val requestedAt : LocalDateTime,
    val respondedAt : LocalDateTime?,
    val scheduledAt : LocalDateTime,
    val location : String,
    val hasFromMemberReview : Boolean,
    val hasToMemberReview : Boolean,

    )
