package kr.co.fitview.api.app.domain.notification.dto.response

import kr.co.fitview.api.app.domain.chat.entity.enums.ChatNoticeMessageType
import java.time.LocalDateTime

data class StompNoticeMessage(
    val chatMessageId: Long,
    val sentAt: LocalDateTime,
    val workoutHistoryId : Long?,
    val content: ChatNoticeMessageType
)
