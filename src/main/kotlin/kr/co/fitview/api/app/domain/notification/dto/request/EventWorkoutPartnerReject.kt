package kr.co.fitview.api.app.domain.notification.dto.request

data class EventWorkoutPartnerReject(
    val memberId : Long,
    val sender : EventSender,
    val payload : EventWorkoutPartnerRejectPayload
)
