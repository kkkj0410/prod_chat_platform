package kr.co.fitview.api.app.domain.notification.mapper

import com.fasterxml.jackson.databind.ObjectMapper
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.notification.dto.response.enums.LinkType
import kr.co.fitview.api.app.domain.notification.dto.response.NotificationLink
import kr.co.fitview.api.app.domain.notification.dto.response.NotificationMessage
import kr.co.fitview.api.app.domain.notification.dto.response.NotificationResponse
import kr.co.fitview.api.app.domain.notification.dto.response.WorkoutPartnerAcceptContent
import kr.co.fitview.api.app.domain.notification.entity.Notification
import kr.co.fitview.api.app.domain.notification.entity.enums.NotificationType
import org.springframework.stereotype.Component

@Component
class WorkoutPartnerAcceptMapper(
    private val objectMapper: ObjectMapper
) : NotificationMapper {

    override fun supportedTypes() = setOf(NotificationType.WORKOUT_PARTNER_ACCEPT)

    override fun map(notification: Notification, member : Member): NotificationResponse {
        val jsonString = objectMapper.writeValueAsString(notification.content)
        val content = objectMapper.readValue(jsonString, WorkoutPartnerAcceptContent::class.java)

        val displayText1 = NotificationType.WORKOUT_PARTNER_ACCEPT.displayText1.format(content.sender.nickname)
        val displayText2 = NotificationType.WORKOUT_PARTNER_ACCEPT.displayText2

        return NotificationResponse(
            notificationId = notification.id!!,
            type = notification.type!!,
            sentAt = notification.sentAt!!,
            isRead = notification.isRead!!,
            sender = content.sender,
            link = NotificationLink(
                type = LinkType.MEMBER_PROFILE,
                parameters = mapOf(
                    "memberId" to content.payload.memberId
                )
            ),
            messages = NotificationMessage(
                text1 = displayText1,
                text2 = displayText2
            )
        )
    }
}