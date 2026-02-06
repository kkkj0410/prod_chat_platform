package kr.co.fitview.api.app.global.stomp.dto.request

data class StompEventTextMessageDepth2(
    val chatRoomId: Long,
    val isCompleteWorkout : Boolean,
    val profileImageUrl: String,
    val nickname: String,
    val chatMessage: StompEventTextMessageDepth3,
)
