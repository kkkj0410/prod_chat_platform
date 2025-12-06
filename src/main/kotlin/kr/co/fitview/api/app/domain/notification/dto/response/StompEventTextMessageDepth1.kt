package kr.co.fitview.api.app.domain.notification.dto.response

data class StompEventTextMessageDepth1 (
    val memberId : Long,
    val message : StompEventTextMessageDepth2
)