package kr.co.fitview.api.app.domain.workout.dto.response

import com.fasterxml.jackson.annotation.JsonIgnore
import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus

data class WorkoutRequestUpdateResponse(
    val chatRoomId : Long,
    val workoutRequestId : Long,
    val status : WorkoutRequestStatus,

    @get:JsonIgnore
    val fromMemberId : Long,
    @get:JsonIgnore
    val toMemberId : Long
)
