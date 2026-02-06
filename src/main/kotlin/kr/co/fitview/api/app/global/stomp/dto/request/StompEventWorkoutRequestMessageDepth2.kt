package kr.co.fitview.api.app.global.stomp.dto.request

data class StompEventWorkoutRequestMessageDepth2(
    val chatRoomId: Long,
    val isCompleteWorkout : Boolean,
    val profileImageUrl: String,
    val nickname: String,
    val chatMessage: StompEventWorkoutRequestMessageDepth3,
)
