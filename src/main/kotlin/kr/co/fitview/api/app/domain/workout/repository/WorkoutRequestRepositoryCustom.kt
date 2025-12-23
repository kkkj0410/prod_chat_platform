package kr.co.fitview.api.app.domain.workout.repository

import kr.co.fitview.api.app.domain.workout.condition.AdminWorkoutRequestCondition
import kr.co.fitview.api.app.domain.workout.dto.response.AdminWorkoutRequestResponse
import kr.co.fitview.api.app.domain.workout.dto.response.LastWorkoutRequestMessage
import kr.co.fitview.api.app.domain.workout.dto.response.WorkoutRequestUpdateResponse
import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequest
import org.springframework.data.domain.Slice

interface WorkoutRequestRepositoryCustom {

    fun findRecentWorkoutRequest(chatRoomIds : List<Long>) : List<LastWorkoutRequestMessage>

    fun findRecentWorkoutRequestEntity(chatRoomId : Long) : WorkoutRequest?

    fun findAllPendingWorkoutRequestAlreadyExpire() : List<WorkoutRequestUpdateResponse>

    fun updateExpireByIdIn(workoutRequestIds: List<Long>)

    fun findWorkoutRequestByIdAndDeletedAtIsNullWithChatMessage(workoutRequestId : Long) : WorkoutRequest?

    fun findAllWorkoutRequestBy(condition: AdminWorkoutRequestCondition): Slice<AdminWorkoutRequestResponse>


}
