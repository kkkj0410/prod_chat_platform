package kr.co.fitview.api.app.domain.notification.mapper

import com.fasterxml.jackson.databind.ObjectMapper
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.notification.dto.response.enums.LinkType
import kr.co.fitview.api.app.domain.notification.dto.response.NotificationLink
import kr.co.fitview.api.app.domain.notification.dto.response.NotificationMessage
import kr.co.fitview.api.app.domain.notification.dto.response.NotificationResponse
import kr.co.fitview.api.app.domain.notification.dto.response.WorkoutRequestRejectContent
import kr.co.fitview.api.app.domain.notification.entity.Notification
import kr.co.fitview.api.app.domain.notification.entity.enums.NotificationType
import org.springframework.stereotype.Component

@Component
class WorkoutRequestRejectMapper(
    private val objectMapper: ObjectMapper
) : NotificationMapper {


    override fun supportedTypes(): Set<NotificationType> {
        return setOf(
            NotificationType.WORKOUT_REQUEST_REJECT,
            NotificationType.WORKOUT_REQUEST_CANCEL,
        )
    }


    override fun map(notification: Notification, member : Member): NotificationResponse {
        val jsonString = objectMapper.writeValueAsString(notification.content)
        val content = objectMapper.readValue(jsonString, WorkoutRequestRejectContent::class.java)

        val displayText1 = NotificationType.WORKOUT_REQUEST_REJECT.displayText1.format(content.sender.nickname)
        val displayText2 = NotificationType.WORKOUT_REQUEST_REJECT.displayText2

        return NotificationResponse(
            notificationId = notification.id!!,
            type = NotificationType.WORKOUT_REQUEST_REJECT,
            sentAt = notification.sentAt!!,
            isRead = notification.isRead!!,
            sender = content.sender,
            link = NotificationLink(
                type = LinkType.CHAT_ROOM,
                parameters = mapOf(
                    "chatRoomId" to content.payload.chatRoomId,
                    "chatMessageId" to content.payload.chatMessageId
                )
            ),
            messages = NotificationMessage(
                text1 = displayText1,
                text2 = displayText2
            )
        )
    }
}