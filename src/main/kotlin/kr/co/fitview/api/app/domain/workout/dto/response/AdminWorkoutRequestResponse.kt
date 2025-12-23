package kr.co.fitview.api.app.domain.workout.dto.response

import com.fasterxml.jackson.annotation.JsonIgnore
import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import java.time.LocalDateTime

data class AdminWorkoutRequestResponse(

    @JsonIgnore
    val fromMemberId: Long,
    @JsonIgnore
    val toMemberId: Long,

    val workoutPartnerId : Long = 0L,
    val workoutRequestId : Long,
    val fromMemberNickname : String,
    val toMemberNickname : String,
    val workoutRequestStatus : WorkoutRequestStatus,
    val requestedAt : LocalDateTime,
    val respondedAt : LocalDateTime? = null,
    val scheduledAt : LocalDateTime,
    val location : String,
    val hasFromMemberReview : Boolean,
    val hasToMemberReview : Boolean,

){

    constructor(
        fromMemberId : Long,
        toMemberId : Long,
        workoutRequestId: Long,
        fromMemberNickname: String,
        toMemberNickname: String,
        workoutRequestStatus: WorkoutRequestStatus,
        requestedAt: LocalDateTime,
        scheduledAt: LocalDateTime,
        location: String,
        hasFromMemberReview : Boolean,
        hasToMemberReview : Boolean,
    ) : this(
        fromMemberId = fromMemberId,
        toMemberId = toMemberId,
        workoutPartnerId = 0L,
        workoutRequestId = workoutRequestId,
        fromMemberNickname = fromMemberNickname,
        toMemberNickname = toMemberNickname,
        workoutRequestStatus = workoutRequestStatus,
        requestedAt = requestedAt,
        respondedAt = null,
        scheduledAt = scheduledAt,
        location = location,
        hasFromMemberReview = hasFromMemberReview,
        hasToMemberReview = hasToMemberReview
    )
}
