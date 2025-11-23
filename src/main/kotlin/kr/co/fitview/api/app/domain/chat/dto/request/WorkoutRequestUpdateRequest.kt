package kr.co.fitview.api.app.domain.chat.dto.request

import kr.co.fitview.api.app.domain.workout.dto.response.enums.WorkoutRequestStatusForRequest

data class WorkoutRequestUpdateRequest(

    val workoutRequestId : Long,

    val status : WorkoutRequestStatusForRequest

)
