package kr.co.fitview.api.app.domain.notification.mapper

import kr.co.fitview.api.app.domain.notification.dto.response.NotificationResponse
import kr.co.fitview.api.app.domain.notification.entity.Notification
import kr.co.fitview.api.app.domain.notification.entity.enums.NotificationType

interface NotificationMapper {
    fun supportedType(): NotificationType
    fun map(notification: Notification): NotificationResponse
}