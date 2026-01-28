package kr.co.fitview.api.app.domain.notification.dto.request

data class EventReviewRequestPayload(
    val workoutHistoryId : Long,
    val chatMessageId : Long,
    val chatRoomId : Long
)