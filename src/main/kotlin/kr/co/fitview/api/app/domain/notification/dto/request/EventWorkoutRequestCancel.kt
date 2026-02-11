package kr.co.fitview.api.app.domain.notification.dto.request

data class EventWorkoutRequestCancel(
    val memberId : Long,
    val sender : EventSender,
    val payload : EventWorkoutRequestCancelPayload
)
