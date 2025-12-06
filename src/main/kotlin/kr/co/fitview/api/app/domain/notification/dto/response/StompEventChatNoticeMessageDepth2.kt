package kr.co.fitview.api.app.domain.notification.dto.response


data class StompEventChatNoticeMessageDepth2(
    val chatRoomId: Long,
    val isCompleteWorkout : Boolean,
    val profileImageUrl: String,
    val nickname: String,
    val chatMessage: StompEventChatNoticeMessageDepth3,
)