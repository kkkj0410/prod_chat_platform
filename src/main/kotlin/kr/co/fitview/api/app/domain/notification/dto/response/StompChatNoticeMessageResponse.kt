package kr.co.fitview.api.app.domain.notification.dto.response

data class StompChatNoticeMessageResponse (
    val memberId : Long,
    val message : StompEventChatNoticeMessage
)