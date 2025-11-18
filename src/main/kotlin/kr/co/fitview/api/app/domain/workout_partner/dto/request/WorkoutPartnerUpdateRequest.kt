package kr.co.fitview.api.app.domain.workout_partner.dto.request

import jakarta.validation.constraints.NotNull
import kr.co.fitview.api.app.domain.workout_partner.dto.request.enums.WorkoutPartnerRequestUpdateStatus

data class WorkoutPartnerUpdateRequest(

    @field:NotNull(message = "type is required")
    val type : WorkoutPartnerRequestUpdateStatus?,

    ) {
    fun toServiceRequest(): WorkoutPartnerUpdateServiceRequest {
        return WorkoutPartnerUpdateServiceRequest(
            type = type!!,
        )
    }
}