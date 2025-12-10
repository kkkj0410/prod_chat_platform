package kr.co.fitview.api.app.domain.notification.dto.request

data class EventWorkoutPartnerRequest(
    val memberId : Long,
    val sender : EventSender,
    val payload : EventWorkoutPartnerRequestPayload
)
