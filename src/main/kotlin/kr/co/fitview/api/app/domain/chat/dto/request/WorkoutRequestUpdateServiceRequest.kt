package kr.co.fitview.api.app.domain.chat.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import kr.co.fitview.api.app.domain.workout.dto.response.enums.WorkoutRequestStatusForRequest

data class WorkoutRequestUpdateServiceRequest(

    val workoutRequestId : Long,
    val status : WorkoutRequestStatusForRequest,
    val clientRequestId : String? = null
)
