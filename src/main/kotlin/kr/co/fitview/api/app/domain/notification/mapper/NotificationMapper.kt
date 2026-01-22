package kr.co.fitview.api.app.domain.notification.mapper

import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.notification.dto.response.NotificationResponse
import kr.co.fitview.api.app.domain.notification.entity.Notification
import kr.co.fitview.api.app.domain.notification.entity.enums.NotificationType

interface NotificationMapper {
    fun supportedTypes(): Set<NotificationType>
    fun map(notification: Notification, member : Member): NotificationResponse
}