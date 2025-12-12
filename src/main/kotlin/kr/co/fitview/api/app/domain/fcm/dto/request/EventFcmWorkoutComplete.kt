package kr.co.fitview.api.app.domain.fcm.dto.request

data class EventFcmWorkoutComplete(
    val toMemberId: Long,
    val chatRoomId: Long,
    val chatMessageId: Long
)