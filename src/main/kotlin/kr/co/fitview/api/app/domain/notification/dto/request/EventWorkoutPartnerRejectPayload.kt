package kr.co.fitview.api.app.domain.notification.dto.request

data class EventWorkoutPartnerRejectPayload(
    val workoutPartnerRequestId : Long,
    val memberId : Long,
)