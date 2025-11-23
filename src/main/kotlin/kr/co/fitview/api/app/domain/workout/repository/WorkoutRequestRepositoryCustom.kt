package kr.co.fitview.api.app.domain.workout.repository

import kr.co.fitview.api.app.domain.workout.dto.response.LastWorkoutRequestMessage
import kr.co.fitview.api.app.domain.workout.dto.response.WorkoutRequestUpdateResponse

interface WorkoutRequestRepositoryCustom {

    fun findRecentWorkoutRequest(chatRoomIds : List<Long>) : List<LastWorkoutRequestMessage>

    fun findAllExpireWorkoutRequest() : List<WorkoutRequestUpdateResponse>

    fun updateExpireByIdIn(workoutRequestIds: List<Long>)

}