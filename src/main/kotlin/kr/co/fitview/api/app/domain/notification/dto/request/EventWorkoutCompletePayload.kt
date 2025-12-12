package kr.co.fitview.api.app.domain.notification.dto.request

data class EventWorkoutCompletePayload(
    val chatMessageId : Long,
    val workoutRequestId : Long,
    val workoutHistoryId : Long,
    val chatRoomId : Long
)