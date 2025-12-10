package kr.co.fitview.api.app.global.stomp.dto.request

data class StompEventWorkoutPartnerRequestDepth1(
    val memberId : Long,
    val message : StompEventWorkoutPartnerRequestDepth2
)

