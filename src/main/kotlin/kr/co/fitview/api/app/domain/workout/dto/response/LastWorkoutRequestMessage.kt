package kr.co.fitview.api.app.domain.workout.dto.response

import kr.co.fitview.api.app.domain.workout.dto.response.enums.WorkoutRequestStatusForResponse
import com.fasterxml.jackson.annotation.JsonIgnore

data class LastWorkoutRequestMessage(
    val workoutRequestId : Long,
    val status : WorkoutRequestStatusForResponse,
    val workoutHistoryId : Long? = null,

    @get:JsonIgnore
    val chatRoomId : Long? = null
)
