package kr.co.fitview.api.app.domain.chat.dto.response

import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageNoticeContent
import kr.co.fitview.api.app.domain.workout.dto.response.enums.WorkoutRequestStatusForResponse
import java.time.LocalDateTime

data class StompChatNoticeMessage(
    override val chatMessageId: Long,
    override val sentAt: LocalDateTime,

    val content: ChatMessageNoticeContent

) : StompChatMessage
