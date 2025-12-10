package kr.co.fitview.api.app.domain.notification.dto.request

data class EventWorkoutRequest(
    val memberId : Long,
    val sender : EventSender,
    val payload : EventWorkoutRequestPayload
)
