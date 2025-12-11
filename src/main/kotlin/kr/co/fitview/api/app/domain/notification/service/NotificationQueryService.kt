package kr.co.fitview.api.app.domain.notification.service

import kr.co.fitview.api.app.domain.notification.condition.NotificationCondition
import kr.co.fitview.api.app.domain.notification.dto.response.NotificationResponse
import kr.co.fitview.api.app.domain.notification.entity.Notification
import kr.co.fitview.api.app.domain.notification.registry.NotificationMapperRegistry
import kr.co.fitview.api.app.domain.notification.repository.NotificationRepository
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class NotificationQueryService(
    private val notificationRepository : NotificationRepository,
    private val mapperRegistry: NotificationMapperRegistry
) {

    fun findAllNotificationFrom(memberId: Long, condition : NotificationCondition) : Slice<NotificationResponse> {
        val findNotifications = notificationRepository.findAllNotificationBy(memberId, condition)

        val content = findNotifications.content.map { mapperRegistry.map(it) }

        return SliceImpl(content, findNotifications.pageable, findNotifications.hasNext())
    }

    fun findNotificationFrom(notificationId : Long) : Notification?{
        return notificationRepository.findByIdAndDeletedAtIsNull(notificationId)
    }

}