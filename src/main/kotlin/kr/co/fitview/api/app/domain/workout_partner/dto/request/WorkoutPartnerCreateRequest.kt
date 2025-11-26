package kr.co.fitview.api.app.domain.workout_partner.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import kr.co.fitview.api.app.domain.oauth2.dto.request.OAuth2LoginServiceRequest
import kr.co.fitview.api.app.domain.workout_partner.entity.enums.WorkoutPartnerRequestContent
import kr.co.fitview.api.app.global.entity.OAuth2Provider

data class WorkoutPartnerCreateRequest(

    @field:NotNull(message = "memberId is required")
    val memberId : Long?,

    @field:NotNull(message = "workoutPartnerRequestContentIndex is required")
    val workoutPartnerRequestContentIndex : WorkoutPartnerRequestContent?,

    ) {
    fun toServiceRequest(): WorkoutPartnerCreateServiceRequest {
        return WorkoutPartnerCreateServiceRequest(
            memberId = memberId!!,
            workoutPartnerRequestContentIndex = workoutPartnerRequestContentIndex!!
        )
    }
}