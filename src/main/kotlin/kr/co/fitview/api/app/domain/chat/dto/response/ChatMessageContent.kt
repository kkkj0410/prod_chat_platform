package kr.co.fitview.api.app.domain.chat.dto.response

import com.fasterxml.jackson.annotation.JsonIgnore
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import java.time.LocalDateTime

data class ChatMessageContent(

    override val chatMessageId: Long,
    override val type: ChatMessageType = ChatMessageType.TEXT,
    override val sentAt: LocalDateTime,
    override val isMe: Boolean = false,

    @get:JsonIgnore
    override val isRead: Boolean?,
    @get:JsonIgnore
    override val chatRoomId: Long?,
    @get:JsonIgnore
    override val memberId: Long?,

    val content: String

) : LastChatMessage
