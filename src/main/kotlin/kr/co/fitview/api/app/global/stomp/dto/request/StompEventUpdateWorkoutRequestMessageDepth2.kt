package kr.co.fitview.api.app.global.stomp.dto.request

import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus

data class StompEventUpdateWorkoutRequestMessageDepth2(
    val chatRoomId : Long,
    val workoutRequestId : Long,
    val status : WorkoutRequestStatus,
    val clientRequestId : String?
)