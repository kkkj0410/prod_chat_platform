package kr.co.fitview.api.app.domain.notification.repository

import kr.co.fitview.api.app.domain.notification.entity.Notification
import org.springframework.data.jpa.repository.JpaRepository

interface NotificationRepository : JpaRepository<Notification, Long> {
}