package kr.co.fitview.api.app.domain.chat.dto.response

import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.LocalDateTime

sealed interface LastChatMessage {
    val chatMessageId: Long
    val type: ChatMessageType
    val sentAt: LocalDateTime

    @get:JsonIgnore
    val isRead : Boolean?
    @get:JsonIgnore
    val chatRoomId : Long?
}