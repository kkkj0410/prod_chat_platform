package kr.co.fitview.api.app.domain.app_feedback.dto.request

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class AppFeedbackPhoneNumberAddRequest(

    @field:NotBlank(message = "phoneNumber is required")
    val phoneNumber : String?

){

    fun toServiceRequest() : AppFeedbackPhoneNumberAddServiceRequest {
        return AppFeedbackPhoneNumberAddServiceRequest(
            phoneNumber = phoneNumber!!,
        )
    }
}