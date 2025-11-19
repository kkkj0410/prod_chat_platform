package kr.co.fitview.api.app.domain.member.dto.response

import kr.co.fitview.api.app.domain.member.dto.response.enums.ProfileWorkoutPartnerStatus

data class WorkoutPartnerStatusResponse(
    val status : ProfileWorkoutPartnerStatus,
    val workoutPartnerRequestId : Long? = null,
    val chatRoomId: Long? = null
)
