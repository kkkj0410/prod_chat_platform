package kr.co.fitview.api.app.domain.fcm.dto.request

data class EventFcmWorkoutPartnerRequest(
    val toMemberId : Long,
    val fromNickname : String,
    val fromMemberId : Long
)