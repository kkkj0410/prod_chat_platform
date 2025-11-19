package kr.co.fitview.api.app.domain.workout_partner.condition

import kr.co.fitview.api.app.domain.workout_partner.dto.request.enums.WorkoutPartnerRequestType

data class WorkoutPartnerRequestCondition(
    val size: Int? = 10,
    val lastWorkoutPartnerRequestId: Long? = null,
    val type : WorkoutPartnerRequestType
)
