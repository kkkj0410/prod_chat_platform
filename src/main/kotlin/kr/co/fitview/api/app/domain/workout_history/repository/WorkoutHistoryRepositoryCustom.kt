package kr.co.fitview.api.app.domain.workout_history.repository

import kr.co.fitview.api.app.domain.workout_history.dto.response.WorkoutHistoryChatRoomResponse

interface WorkoutHistoryRepositoryCustom{

    fun findWorkoutHistoryBy(chatRoomIds : List<Long>): List<WorkoutHistoryChatRoomResponse>

}
