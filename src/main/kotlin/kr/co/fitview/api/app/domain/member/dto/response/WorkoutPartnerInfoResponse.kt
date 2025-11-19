package kr.co.fitview.api.app.domain.member.dto.response

import kr.co.fitview.api.app.domain.member.dto.response.enums.ProfileWorkoutPartnerStatus

data class WorkoutPartnerInfoResponse(
    val status : ProfileWorkoutPartnerStatus,
    val workoutPartnerRequestId : Long?,
    val chatRoomId: Long?
)
