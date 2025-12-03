package kr.co.fitview.api.app.domain.chat.dto.response

import com.fasterxml.jackson.annotation.JsonIgnore
import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import java.time.LocalDateTime

data class ChatMessageContent(

    override val chatMessageId: Long,
    override val type: ChatMessageType = ChatMessageType.TEXT,
    override val sentAt: LocalDateTime,

    val isMe: Boolean = false,
    @get:JsonIgnore
    override val isRead: Boolean? = null,
    @get:JsonIgnore
    override val chatRoomId: Long? = null,
    @get:JsonIgnore
    val memberId: Long? = null,

    val content: String

) : LastChatMessage{


    companion object {
        fun from(chatMessage: ChatMessage, myMemberId : Long): ChatMessageContent {
            return ChatMessageContent(
                chatMessageId = chatMessage.id!!,
                type = chatMessage.type!!,
                sentAt = chatMessage.sentAt!!,
                isMe = myMemberId == chatMessage.getMemberId(),
                content = chatMessage.content ?: ""
            )
        }
    }
}

