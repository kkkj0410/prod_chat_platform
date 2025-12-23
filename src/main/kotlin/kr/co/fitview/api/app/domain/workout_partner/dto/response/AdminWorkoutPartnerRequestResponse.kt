package kr.co.fitview.api.app.domain.workout_partner.dto.response

import com.fasterxml.jackson.annotation.JsonIgnore
import kr.co.fitview.api.app.domain.workout_partner.dto.response.enums.WorkoutPartnerRequestStatusForResponse
import kr.co.fitview.api.app.domain.workout_partner.entity.enums.WorkoutPartnerRequestStatus
import java.time.LocalDateTime

data class AdminWorkoutPartnerRequestResponse(

    @JsonIgnore
    val fromMemberId: Long,
    @JsonIgnore
    val toMemberId: Long,

    val workoutPartnerRequestId : Long,
    val fromMemberNickname : String,
    val toMemberNickname : String,
    val workoutPartnerRequestStatus : WorkoutPartnerRequestStatus,
    val requestedAt : LocalDateTime,
    val respondedAt : LocalDateTime?,
    val hasChatRoom : Boolean,
    val workoutHistoryCount : Long

)
{
    constructor(
        fromMemberId: Long,
        toMemberId: Long,
        workoutPartnerRequestId: Long,
        fromMemberNickname: String,
        toMemberNickname: String,
        workoutPartnerRequestStatus: WorkoutPartnerRequestStatus,
        requestedAt: LocalDateTime,
        respondedAt: LocalDateTime?
    ) : this(
        fromMemberId = fromMemberId,
        toMemberId = toMemberId,
        workoutPartnerRequestId = workoutPartnerRequestId,
        fromMemberNickname = fromMemberNickname,
        toMemberNickname = toMemberNickname,
        workoutPartnerRequestStatus = workoutPartnerRequestStatus,
        requestedAt = requestedAt,
        respondedAt = respondedAt,
        hasChatRoom = false,
        workoutHistoryCount = 0L
    )
}