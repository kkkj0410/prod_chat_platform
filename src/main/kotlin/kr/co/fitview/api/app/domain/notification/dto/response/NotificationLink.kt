package kr.co.fitview.api.app.domain.notification.dto.response

import kr.co.fitview.api.app.domain.notification.dto.response.enums.LinkType

data class NotificationLink(
    val type: LinkType,
    val parameters: Map<String, Any>?
)
