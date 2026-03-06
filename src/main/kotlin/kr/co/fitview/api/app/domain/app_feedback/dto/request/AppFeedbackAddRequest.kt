package kr.co.fitview.api.app.domain.app_feedback.dto.request

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class AppFeedbackAddRequest(

    @field:NotNull(message = "rating is required")
    @field:Min(value = 1, message = "rating must be at least 1")
    @field:Max(value = 5, message = "rating must be at most 5")
    val rating : Int?,

    @field:NotBlank(message = "painPoint is required")
    val painPoint : String?,

    val improvement : String?



){
    fun toServiceRequest() : AppFeedbackAddServiceRequest {
        return AppFeedbackAddServiceRequest(
            rating = rating!!,
            painPoint = painPoint!!,
            improvement = improvement
        )
    }
}