package kr.co.fitview.api.app.domain.notification.dto.response

data class NotificationLink(
    val type: LinkType,
    val parameters: Map<String, Any>?
)
