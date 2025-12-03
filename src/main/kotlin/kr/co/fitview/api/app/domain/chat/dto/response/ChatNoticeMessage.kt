package kr.co.fitview.api.app.domain.chat.dto.response

import com.fasterxml.jackson.annotation.JsonIgnore
import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageNoticeContent
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import java.time.LocalDateTime

data class ChatNoticeMessage(

    override val chatMessageId: Long,
    override val type: ChatMessageType = ChatMessageType.NOTICE,
    override val sentAt: LocalDateTime,

    @get:JsonIgnore
    override val isRead: Boolean? = null,
    @get:JsonIgnore
    override val chatRoomId: Long? = null,

    val content: ChatMessageNoticeContent

) : LastChatMessage{


    companion object {
        fun from(chatMessage: ChatMessage): ChatNoticeMessage {
            return ChatNoticeMessage(
                chatMessageId = chatMessage.id!!,
                type = chatMessage.type!!,
                sentAt = chatMessage.sentAt!!,
                content = ChatMessageNoticeContent.valueOf(chatMessage.content!!)
            )
        }
    }
}

