package kr.co.fitview.api.app.domain.workout_partner.dto.response

import kr.co.fitview.api.app.domain.workout_partner.dto.response.enums.WorkoutPartnerRequestStatusForResponse
import kr.co.fitview.api.app.domain.workout_partner.entity.enums.WorkoutPartnerRequestStatus
import java.time.LocalDateTime

data class AdminWorkoutPartnerRequestResponse(

    val workoutPartnerRequestId : Long,
    val fromMemberNickname : String,
    val toMemberNickname : String,
    val workoutPartnerRequestStatus : WorkoutPartnerRequestStatusForResponse,
    val requestedAt : LocalDateTime,
    val respondedAt : LocalDateTime?,
    val hasChatRoom : Boolean,
    val workoutHistoryCount : Long

)
