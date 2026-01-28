package kr.co.fitview.api.app.domain.workout_history.repository

import kr.co.fitview.api.app.domain.workout_history.dto.response.WorkoutHistoryAndChatMessage
import kr.co.fitview.api.app.domain.workout_history.dto.response.WorkoutHistoryChatRoomResponse
import kr.co.fitview.api.app.domain.workout_history.entity.WorkoutHistory

interface WorkoutHistoryRepositoryCustom{

    fun findWorkoutHistoryBy(chatRoomIds : List<Long>): List<WorkoutHistoryChatRoomResponse>

    fun findAllWorkoutHistoryExceed24HoursWithoutReview(): List<WorkoutHistoryAndChatMessage>

}
