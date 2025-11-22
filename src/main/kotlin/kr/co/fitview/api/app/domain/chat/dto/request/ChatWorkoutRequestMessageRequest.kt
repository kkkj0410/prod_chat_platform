package kr.co.fitview.api.app.domain.chat.dto.request

import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import java.time.LocalDateTime

data class ChatWorkoutRequestMessageRequest(
    override val type: ChatMessageType = ChatMessageType.WORKOUT_REQUEST,

    val scheduledAt : LocalDateTime?,
    val location : String?

) : ChatMessageRequest{

    fun toServiceRequest(): ChatWorkoutRequestMessageServiceRequest {
        return ChatWorkoutRequestMessageServiceRequest(
            type = type,
            scheduledAt = scheduledAt!!,
            location = location!!
        )
    }
}
