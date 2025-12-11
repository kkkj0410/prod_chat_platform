package kr.co.fitview.api.app.domain.fcm.dto.request

data class EventFcmWorkoutPartnerAccept(
    val toMemberId: Long,
    val fromMemberId: Long
)