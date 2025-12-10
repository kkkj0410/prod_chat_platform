package kr.co.fitview.api.app.domain.notification.dto.request

data class EventReviewReceive(
    val memberId : Long,
    val sender : EventSender,
    val payload : EventReviewReceivePayload
)
