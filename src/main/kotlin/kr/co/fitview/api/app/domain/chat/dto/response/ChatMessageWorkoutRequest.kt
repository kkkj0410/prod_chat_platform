package kr.co.fitview.api.app.domain.chat.dto.response

import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import java.time.LocalDateTime

data class ChatMessageWorkoutRequest(

    override val chatMessageId: Long,
    override val type: ChatMessageType = ChatMessageType.TEXT,
    override val createdAt: LocalDateTime,
    override val isMe: Boolean,

    val workoutRequestId: Long,
    val status: String,
    val scheduledAt: String,
    val location: String
    
) : LastChatMessage
