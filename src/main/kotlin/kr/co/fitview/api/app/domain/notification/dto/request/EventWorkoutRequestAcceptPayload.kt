package kr.co.fitview.api.app.domain.notification.dto.request

data class EventWorkoutRequestAcceptPayload(
    val chatMessageId : Long,
    val workoutRequestId : Long,
    val chatRoomId : Long,
)