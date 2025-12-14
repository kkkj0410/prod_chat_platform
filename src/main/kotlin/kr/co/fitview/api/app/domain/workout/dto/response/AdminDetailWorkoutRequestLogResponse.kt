package kr.co.fitview.api.app.domain.workout.dto.response

import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import java.time.LocalDateTime

data class AdminDetailWorkoutRequestLogResponse(

    val workoutRequestStatus : WorkoutRequestStatus,
    val loggedAt : LocalDateTime,
    val fromMemberNickname : String,
    val toMemberNickname : String,


)
