package kr.co.fitview.api.app.domain.workout.repository

import kr.co.fitview.api.app.domain.workout.dto.response.WorkoutHistoryChatRoomResponse

interface WorkoutHistoryRepositoryCustom{

    fun findWorkoutHistoryBy(chatRoomIds : List<Long>): List<WorkoutHistoryChatRoomResponse>

}
