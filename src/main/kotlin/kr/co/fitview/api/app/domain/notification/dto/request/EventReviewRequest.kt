package kr.co.fitview.api.app.domain.notification.dto.request

data class EventReviewRequest(
    val memberId : Long,
    val sender : EventSender,
    val payload : EventReviewRequestPayload
)
