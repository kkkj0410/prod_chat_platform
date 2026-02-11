package kr.co.fitview.api.app.domain.notification.mapper

import com.fasterxml.jackson.databind.ObjectMapper
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.notification.dto.response.*
import kr.co.fitview.api.app.domain.notification.dto.response.enums.LinkType
import kr.co.fitview.api.app.domain.notification.entity.Notification
import kr.co.fitview.api.app.domain.notification.entity.enums.NotificationType
import org.springframework.stereotype.Component

@Component
class ReviewRequestMapper(
    private val objectMapper: ObjectMapper
) : NotificationMapper {

    override fun supportedTypes() = setOf(NotificationType.REVIEW_REQUEST)

    override fun map(notification: Notification, member : Member, sender : NotificationSender): NotificationResponse {
        val jsonString = objectMapper.writeValueAsString(notification.content)
        val content = objectMapper.readValue(jsonString, ReviewRequestContent::class.java)

        val displayText1 = NotificationType.REVIEW_REQUEST.displayText1.format(member.nickname!!)
        val displayText2 = NotificationType.REVIEW_REQUEST.displayText2.format(sender.nickname)

        return NotificationResponse(
            notificationId = notification.id!!,
            type = notification.type!!,
            sentAt = notification.sentAt!!,
            isRead = notification.isRead!!,
            sender = sender,
            link = NotificationLink(
                type = LinkType.CHAT_ROOM,
                parameters = mapOf(
                    "chatMessageId" to content.payload.chatMessageId,
                    "chatRoomId" to content.payload.chatRoomId
                )
            ),
            messages = NotificationMessage(
                text1 = displayText1,
                text2 = displayText2
            )
        )
    }
}