package kr.co.fitview.api.app.global.stomp.dto.request

data class StompEventChatNoticeMessageDepth1 (
    val memberId : Long,
    val message : StompEventChatNoticeMessageDepth2
)