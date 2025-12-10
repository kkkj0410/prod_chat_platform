package kr.co.fitview.api.app.global.stomp.dto.request

import java.time.LocalDateTime

data class StompEventTextMessageDepth3(
    val chatMessageId: Long,
    val content : String,
    val sentAt : LocalDateTime,
    val isMe : Boolean
)
