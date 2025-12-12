package kr.co.fitview.api.app.domain.notification.mapper

import com.fasterxml.jackson.databind.ObjectMapper
import kr.co.fitview.api.app.domain.notification.dto.response.*
import kr.co.fitview.api.app.domain.notification.dto.response.enums.LinkType
import kr.co.fitview.api.app.domain.notification.entity.Notification
import kr.co.fitview.api.app.domain.notification.entity.enums.NotificationType
import org.springframework.stereotype.Component

@Component
class WorkoutPartnerRequestMapper(
    private val objectMapper: ObjectMapper
) : NotificationMapper {

    override fun supportedType() = NotificationType.WORKOUT_PARTNER_REQUEST

    override fun map(notification: Notification): NotificationResponse {
        val jsonString = objectMapper.writeValueAsString(notification.content)
        val content = objectMapper.readValue(jsonString, WorkoutPartnerRequestContent::class.java)
        return NotificationResponse(
            notificationId = notification.id!!,
            type = notification.type!!,
            sentAt = notification.sentAt!!,
            isRead = notification.isRead!!,
            sender = NotificationSender(
                memberId = content.sender.memberId,
                nickname = content.sender.nickname,
                profileImageUrl = content.sender.profileImageUrl
            ),
            link = NotificationLink(
                type = LinkType.MEMBER_PROFILE,
                parameters = mapOf("memberId" to content.payload.memberId)
            )
        )
    }
}