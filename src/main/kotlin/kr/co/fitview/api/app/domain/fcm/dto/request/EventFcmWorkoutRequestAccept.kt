package kr.co.fitview.api.app.domain.fcm.dto.request

data class EventFcmWorkoutRequestAccept(
    val toMemberId: Long,
    val chatRoomId: Long,
    val chatMessageId: Long
)