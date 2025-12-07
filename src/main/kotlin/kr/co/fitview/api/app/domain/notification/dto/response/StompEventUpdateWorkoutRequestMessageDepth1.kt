package kr.co.fitview.api.app.domain.notification.dto.response

import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus

data class StompEventUpdateWorkoutRequestMessageDepth1(
    val memberId : Long,
    val message : StompEventUpdateWorkoutRequestMessageDepth2,
)