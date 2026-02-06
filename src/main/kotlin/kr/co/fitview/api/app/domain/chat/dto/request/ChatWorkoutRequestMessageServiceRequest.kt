package kr.co.fitview.api.app.domain.chat.dto.request

import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import java.time.LocalDateTime

data class ChatWorkoutRequestMessageServiceRequest(
    val type: ChatMessageType = ChatMessageType.WORKOUT_REQUEST,
    val scheduledAt : LocalDateTime,
    val location : String
)
