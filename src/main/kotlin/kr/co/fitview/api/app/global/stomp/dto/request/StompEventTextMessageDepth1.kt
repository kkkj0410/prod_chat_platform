package kr.co.fitview.api.app.global.stomp.dto.request

data class StompEventTextMessageDepth1 (
    val memberId : Long,
    val message : StompEventTextMessageDepth2
)