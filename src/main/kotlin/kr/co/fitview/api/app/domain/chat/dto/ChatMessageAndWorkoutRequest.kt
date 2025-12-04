package kr.co.fitview.api.app.domain.chat.dto

import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatNoticeMessage
import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequest

data class ChatMessageAndWorkoutRequest(
    val chatMessage : ChatMessage,
    val workoutRequest : WorkoutRequest?,
    val chatNoticeMessage : ChatNoticeMessage?
)