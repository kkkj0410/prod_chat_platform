package kr.co.fitview.api.app.domain.workout_partner.dto.request

import kr.co.fitview.api.app.domain.workout_partner.dto.request.enums.WorkoutPartnerRequestUpdateStatus

data class WorkoutPartnerUpdateServiceRequest(
    val type : WorkoutPartnerRequestUpdateStatus,
)
