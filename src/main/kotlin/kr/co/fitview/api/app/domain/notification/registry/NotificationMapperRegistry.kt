package kr.co.fitview.api.app.domain.notification.registry

import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.notification.dto.response.NotificationResponse
import kr.co.fitview.api.app.domain.notification.entity.Notification
import kr.co.fitview.api.app.domain.notification.entity.enums.NotificationType
import kr.co.fitview.api.app.domain.notification.mapper.NotificationMapper
import org.springframework.stereotype.Component

@Component
class NotificationMapperRegistry(
    mappers: List<NotificationMapper>
) {

    private val mapperMap: Map<NotificationType, NotificationMapper> =
        mappers.associateBy { it.supportedType() }

    fun map(notification: Notification, member : Member): NotificationResponse {
        val mapper = mapperMap[notification.type]
            ?: throw IllegalArgumentException("No mapper for ${notification.type}")
        return mapper.map(notification, member)
    }
}