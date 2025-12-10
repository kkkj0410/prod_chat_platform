package kr.co.fitview.api.app.domain.notification.dto.request

data class EventWorkoutRequestAcceptPayload(
    val workoutRequestId : Long,
    val chatRoomId : Long,
)