package kr.co.fitview.api.app.domain.notification.dto.request

data class EventReviewReceivePayload(
    val reviewId : Long,
    val workoutHistoryId : Long,
    val chatRoomId : Long
)