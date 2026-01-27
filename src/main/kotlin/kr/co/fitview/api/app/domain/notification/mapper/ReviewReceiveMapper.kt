package kr.co.fitview.api.app.domain.notification.mapper

import com.fasterxml.jackson.databind.ObjectMapper
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.notification.dto.response.*
import kr.co.fitview.api.app.domain.notification.dto.response.enums.LinkType
import kr.co.fitview.api.app.domain.notification.entity.Notification
import kr.co.fitview.api.app.domain.notification.entity.enums.NotificationType
import org.springframework.stereotype.Component

@Component
class ReviewReceiveMapper(
    private val objectMapper: ObjectMapper
) : NotificationMapper {

    override fun supportedTypes() = setOf(NotificationType.REVIEW_RECEIVE)

    override fun map(notification: Notification, member : Member, sender : NotificationSender): NotificationResponse {
//        val jsonString = objectMapper.writeValueAsString(notification.content)
//        val content = objectMapper.readValue(jsonString, ReviewReceiveContent::class.java)

        val displayText1 = NotificationType.REVIEW_RECEIVE.displayText1.format(sender.nickname)
        val displayText2 = NotificationType.REVIEW_RECEIVE.displayText2

        return NotificationResponse(
            notificationId = notification.id!!,
            type = notification.type!!,
            sentAt = notification.sentAt!!,
            isRead = notification.isRead!!,
            sender = sender,
            link = NotificationLink(
                type = LinkType.MEMBER_PROFILE_ME,
                parameters = null
            ),
            messages = NotificationMessage(
                text1 = displayText1,
                text2 = displayText2
            )
        )
    }
}