package kr.co.fitview.api.app.domain.notification.dto.request

data class EventWorkoutRequestReject(
    val memberId : Long,
    val sender : EventSender,
    val payload : EventWorkoutRequestRejectPayload
)
