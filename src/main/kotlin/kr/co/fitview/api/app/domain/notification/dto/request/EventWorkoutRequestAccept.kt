package kr.co.fitview.api.app.domain.notification.dto.request

data class EventWorkoutRequestAccept(
    val memberId : Long,
    val sender : EventSender,
    val payload : EventWorkoutRequestAcceptPayload
)
