package kr.co.fitview.api.app.domain.notification.dto.response


data class StompEventChatNoticeMessage(
    val chatRoomId: Long,
    val isCompleteWorkout : Boolean,
    val profileImageUrl: String,
    val nickname: String,
    val chatMessage: StompNoticeMessage,
)