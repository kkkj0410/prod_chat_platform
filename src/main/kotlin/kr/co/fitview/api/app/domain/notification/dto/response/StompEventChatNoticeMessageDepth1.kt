package kr.co.fitview.api.app.domain.notification.dto.response

data class StompEventChatNoticeMessageDepth1 (
    val memberId : Long,
    val message : StompEventChatNoticeMessageDepth2
)