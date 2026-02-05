package kr.co.fitview.api.app.domain.chat.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import java.time.LocalDateTime

data class ChatWorkoutRequestMessageRequest(

    @field:NotNull(message = "type is required")
    override val type: ChatMessageType = ChatMessageType.WORKOUT_REQUEST,

    override val clientRequestId: String? = null,

    @field:NotNull(message = "scheduledAt is required")
    val scheduledAt : LocalDateTime?,

    @field:NotBlank(message = "location is required")
    val location : String?

) : ChatMessageRequest{

    fun toServiceRequest(): ChatWorkoutRequestMessageServiceRequest {
        return ChatWorkoutRequestMessageServiceRequest(
            type = type,
            clientRequestId = clientRequestId,
            scheduledAt = scheduledAt!!,
            location = location!!
        )
    }
}
