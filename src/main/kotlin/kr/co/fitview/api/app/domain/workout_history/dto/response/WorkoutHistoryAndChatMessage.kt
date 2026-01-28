package kr.co.fitview.api.app.domain.workout_history.dto.response

import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.workout_history.entity.WorkoutHistory

data class WorkoutHistoryAndChatMessage(

    val workoutHistory : WorkoutHistory,
    val chatMessage : ChatMessage

)
