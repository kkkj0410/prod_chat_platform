package kr.co.fitview.api.app.domain.workout_partner.dto.response

import kr.co.fitview.api.app.domain.workout_partner.entity.enums.WorkoutPartnerRequestStatus

data class LastWorkoutPartnerRequestResponse(
    val workoutPartnerRequestId : Long,
    val status : WorkoutPartnerRequestStatus,
)