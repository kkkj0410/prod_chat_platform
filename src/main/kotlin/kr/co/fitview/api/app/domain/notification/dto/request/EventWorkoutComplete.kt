package kr.co.fitview.api.app.domain.notification.dto.request

data class EventWorkoutComplete(
    val memberId : Long,
    val sender : EventSender,
    val payload : EventWorkoutCompletePayload
)
