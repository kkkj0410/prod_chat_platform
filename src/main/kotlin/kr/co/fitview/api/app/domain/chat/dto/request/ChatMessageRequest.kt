package kr.co.fitview.api.app.domain.chat.dto.request

import jakarta.validation.constraints.NotBlank
import kr.co.fitview.api.app.domain.auth.dto.request.AccessTokenRefreshServiceRequest

data class ChatMessageRequest(

    @field:NotBlank(message = "message is required")
    val message : String?,

    @field:NotBlank(message = "senderEmail is required")
    val senderEmail : String?

){
    fun toServiceRequest() : ChatMessageServiceRequest {
        return ChatMessageServiceRequest(
            message = message!!,
            senderEmail = senderEmail!!
        )
    }
}
