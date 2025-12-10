package kr.co.fitview.api.app.domain.notification.mapper

import com.fasterxml.jackson.databind.ObjectMapper
import kr.co.fitview.api.app.domain.notification.dto.response.LinkType
import kr.co.fitview.api.app.domain.notification.dto.response.NotificationLink
import kr.co.fitview.api.app.domain.notification.dto.response.NotificationResponse
import kr.co.fitview.api.app.domain.notification.dto.response.WorkoutCompleteContent
import kr.co.fitview.api.app.domain.notification.entity.Notification
import kr.co.fitview.api.app.domain.notification.entity.enums.NotificationType
import org.springframework.stereotype.Component

@Component
class WorkoutCompleteMapper(
    private val objectMapper: ObjectMapper
) : NotificationMapper {

    override fun supportedType() = NotificationType.WORKOUT_COMPLETE

    override fun map(notification: Notification): NotificationResponse {
        val jsonString = objectMapper.writeValueAsString(notification.content)
        val content = objectMapper.readValue(jsonString, WorkoutCompleteContent::class.java)
        return NotificationResponse(
            notificationId = notification.id!!,
            type = notification.type!!,
            sentAt = notification.sentAt!!,
            isRead = notification.isRead!!,
            sender = content.sender,
            link = NotificationLink(
                type = LinkType.REVIEW_WRITE,
                parameters = mapOf(
                    "workoutHistoryId" to content.payload.workoutHistoryId,
                    "chatRoomId" to content.payload.chatRoomId
                )
            )
        )
    }
}