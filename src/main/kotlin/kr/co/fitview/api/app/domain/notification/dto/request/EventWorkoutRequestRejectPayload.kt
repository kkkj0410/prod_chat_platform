package kr.co.fitview.api.app.domain.notification.dto.request

data class EventWorkoutRequestRejectPayload(
    val chatMessageId : Long,
    val workoutRequestId : Long,
    val chatRoomId : Long
)