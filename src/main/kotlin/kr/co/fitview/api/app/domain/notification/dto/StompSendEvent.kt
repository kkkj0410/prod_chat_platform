package kr.co.fitview.api.app.domain.notification.dto

import kr.co.fitview.api.app.global.dto.WsResponse

data class StompSendEvent(
    val memberId: Long,
    val destination: String,
    val payload: WsResponse<Any>
)