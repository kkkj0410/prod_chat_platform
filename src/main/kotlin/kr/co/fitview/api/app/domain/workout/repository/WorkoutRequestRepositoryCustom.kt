package kr.co.fitview.api.app.domain.workout.repository

import kr.co.fitview.api.app.domain.workout.dto.response.LastWorkoutRequestMessage
import kr.co.fitview.api.app.domain.workout.dto.response.WorkoutRequestUpdateResponse
import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequest

interface WorkoutRequestRepositoryCustom {

    fun findRecentWorkoutRequest(chatRoomIds : List<Long>) : List<LastWorkoutRequestMessage>

    fun findAllPendingWorkoutRequestAlreadyExpire() : List<WorkoutRequestUpdateResponse>

    fun updateExpireByIdIn(workoutRequestIds: List<Long>)

    fun findWorkoutRequestByIdAndDeletedAtIsNullWithChatMessage(workoutRequestId : Long) : WorkoutRequest?


}