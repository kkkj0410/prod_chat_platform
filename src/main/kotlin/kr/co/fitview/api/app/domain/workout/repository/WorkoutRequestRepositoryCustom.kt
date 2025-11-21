package kr.co.fitview.api.app.domain.workout.repository

import kr.co.fitview.api.app.domain.workout.dto.response.LastWorkoutRequestMessage

interface WorkoutRequestRepositoryCustom {

    fun findRecentWorkoutRequest(chatRoomIds : List<Long>) : List<LastWorkoutRequestMessage>

}