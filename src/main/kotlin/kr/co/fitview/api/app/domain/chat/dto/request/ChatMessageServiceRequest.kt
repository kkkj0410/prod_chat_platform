package kr.co.fitview.api.app.domain.chat.dto.request

data class ChatMessageServiceRequest(
    val message : String,
    val senderEmail : String
)
