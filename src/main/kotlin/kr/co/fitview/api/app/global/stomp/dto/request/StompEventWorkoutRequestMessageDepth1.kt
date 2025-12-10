package kr.co.fitview.api.app.global.stomp.dto.request

data class StompEventWorkoutRequestMessageDepth1 (
    val memberId : Long,
    val message : StompEventWorkoutRequestMessageDepth2
)