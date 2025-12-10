package kr.co.fitview.api.app.domain.notification.dto.request

data class EventWorkoutPartnerAcceptPayload(
    val workoutPartnerRequestId : Long,
    val workoutPartnerId : Long,
    val memberId : Long
)