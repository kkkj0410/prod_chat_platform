package kr.co.fitview.api.app.domain.fcm.dto.request

data class EventFcmChatMessage(
    val toMemberId: Long,
    val fromNickname: String,
    val chatRoomId: Long,
    val chatMessageId: Long
)