package kr.co.fitview.api.app.domain.chat.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import kr.co.fitview.api.app.domain.workout.dto.response.enums.WorkoutRequestStatusForRequest

data class WorkoutRequestUpdateRequest(

    @field:NotNull(message = "workoutRequestId is required")
    val workoutRequestId : Long?,

    @field:NotNull(message = "status is required")
    val status : WorkoutRequestStatusForRequest?,

){

    fun toServiceRequest(): WorkoutRequestUpdateServiceRequest {
        return WorkoutRequestUpdateServiceRequest(
            workoutRequestId = workoutRequestId!!,
            status = status!!,
        )
    }
}
