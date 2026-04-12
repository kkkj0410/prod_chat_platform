package kr.co.fitview.api.app.domain.workout_history.repository

import kr.co.fitview.api.app.domain.workout_history.dto.response.WorkoutHistoryAndChatMessage
import kr.co.fitview.api.app.domain.workout_history.dto.response.WorkoutHistoryChatRoomResponse
import kr.co.fitview.api.app.domain.workout_history.dto.response.WorkoutHistoryRecentResponse
import kr.co.fitview.api.app.domain.workout_history.entity.WorkoutHistory
import java.time.LocalDate

interface WorkoutHistoryRepositoryCustom{

    fun findWorkoutHistoryBy(chatRoomIds : List<Long>): List<WorkoutHistoryChatRoomResponse>

    fun findAllWorkoutHistoryExceed24HoursWithoutReview(): List<WorkoutHistoryAndChatMessage>

    fun findAllWorkoutHistoryBy(memberId: Long, startDate : LocalDate, limit : Int): List<WorkoutHistoryRecentResponse>

}
