package kr.co.fitview.api.app.domain.notification.dto.request

data class EventWorkoutRequestPayload(
    val workoutRequestId : Long,
    val chatRoomId : Long,
)