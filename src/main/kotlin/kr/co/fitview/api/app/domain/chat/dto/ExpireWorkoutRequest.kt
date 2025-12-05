package kr.co.fitview.api.app.domain.chat.dto

data class ExpireWorkoutRequest(
    val memberOneId : Long,
    val memberTwoId : Long,
    val chatRoomId : Long
)