package kr.co.fitview.api.app.domain.chat.dto.request

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType


@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
    visible = true
)
@JsonSubTypes(
    JsonSubTypes.Type(value = ChatTextMessageRequest::class, name = "TEXT"),
    JsonSubTypes.Type(value = ChatWorkoutRequestMessageRequest::class, name = "WORKOUT_REQUEST")
)
sealed interface ChatMessageRequest {
//    val type: ChatMessageType?
    val type: ChatMessageType

    val clientRequestId : String?
}