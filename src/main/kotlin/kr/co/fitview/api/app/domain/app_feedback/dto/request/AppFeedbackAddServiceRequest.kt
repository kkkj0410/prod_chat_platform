package kr.co.fitview.api.app.domain.app_feedback.dto.request

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class AppFeedbackAddServiceRequest(

    val rating : Int,
    val painPoint : String,
    val improvement : String?

)