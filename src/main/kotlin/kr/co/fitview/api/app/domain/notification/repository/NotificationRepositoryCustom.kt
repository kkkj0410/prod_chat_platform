package kr.co.fitview.api.app.domain.notification.repository

import kr.co.fitview.api.app.domain.notification.condition.NotificationCondition
import kr.co.fitview.api.app.domain.notification.dto.response.NotificationResponse
import kr.co.fitview.api.app.domain.notification.entity.Notification
import org.springframework.data.domain.Slice

interface NotificationRepositoryCustom {
    fun findAllNotificationBy(memberId: Long, condition : NotificationCondition): Slice<Notification>

}