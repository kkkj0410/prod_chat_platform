package kr.co.fitview.api.app.domain.chat.dto.response

import kr.co.fitview.api.app.domain.workout_partner.dto.response.enums.WorkoutPartnerRequestStatusForResponse

data class LastWorkoutRequestMessage(
    val status : WorkoutPartnerRequestStatusForResponse
)
