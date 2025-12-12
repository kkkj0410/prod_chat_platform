package kr.co.fitview.api.app.domain.notification.dto.response

import kr.co.fitview.api.app.domain.notification.entity.enums.NotificationType
import java.time.LocalDateTime

data class NotificationResponse(
    val notificationId: Long,
    val type: NotificationType,
    val sentAt: LocalDateTime,
    val isRead: Boolean,
    val sender: NotificationSender,
    val link: NotificationLink,
    val messages : NotificationMessage
)