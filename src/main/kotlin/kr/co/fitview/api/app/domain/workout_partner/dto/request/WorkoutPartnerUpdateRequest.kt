package kr.co.fitview.api.app.domain.workout_partner.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import kr.co.fitview.api.app.domain.oauth2.dto.request.OAuth2LoginServiceRequest
import kr.co.fitview.api.app.domain.workout_partner.dto.request.enums.WorkoutPartnerUpdateType
import kr.co.fitview.api.app.global.entity.OAuth2Provider

data class WorkoutPartnerUpdateRequest(

    @field:NotNull(message = "type is required")
    val type : WorkoutPartnerUpdateType?,

    ) {
    fun toServiceRequest(): WorkoutPartnerUpdateServiceRequest {
        return WorkoutPartnerUpdateServiceRequest(
            type = type!!,
        )
    }
}