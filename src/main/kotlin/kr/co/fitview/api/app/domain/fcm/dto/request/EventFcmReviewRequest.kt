package kr.co.fitview.api.app.domain.fcm.dto.request

data class EventFcmReviewRequest(
    val toMemberId : Long,
    val workoutHistoryId : Long
)