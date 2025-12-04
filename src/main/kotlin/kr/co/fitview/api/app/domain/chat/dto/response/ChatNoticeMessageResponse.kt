package kr.co.fitview.api.app.domain.chat.dto.response

import com.fasterxml.jackson.annotation.JsonIgnore
import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatNoticeMessage
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatNoticeMessageType
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import java.time.LocalDateTime

data class ChatNoticeMessageResponse(

    override val chatMessageId: Long,
    override val type: ChatMessageType = ChatMessageType.NOTICE,
    override val sentAt: LocalDateTime,

    @get:JsonIgnore
    override val isRead: Boolean? = null,
    @get:JsonIgnore
    override val chatRoomId: Long? = null,

    val content: ChatNoticeMessageType,
    val workoutHistoryId : Long? = null

) : LastChatMessage{


    companion object {
        fun from(chatMessage: ChatMessage, chatNoticeMessage : ChatNoticeMessage): ChatNoticeMessageResponse {
            return ChatNoticeMessageResponse(
                chatMessageId = chatMessage.id!!,
                type = chatMessage.type!!,
                sentAt = chatMessage.sentAt!!,
                content = chatNoticeMessage.type!!,
                workoutHistoryId = chatNoticeMessage.getWorkoutHistoryId
            )
        }
    }
}

