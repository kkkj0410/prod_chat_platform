package kr.co.fitview.api.app.domain.fcm.dto.request

import java.time.LocalDateTime

data class EventFcmWorkoutRequest(
    val toMemberId: Long,
    val fromNickname: String,
    val scheduledAt : LocalDateTime,
    val chatRoomId: Long,
    val chatMessageId: Long
)