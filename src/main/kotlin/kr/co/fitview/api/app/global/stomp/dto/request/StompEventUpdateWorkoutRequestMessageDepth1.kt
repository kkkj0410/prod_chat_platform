package kr.co.fitview.api.app.global.stomp.dto.request

data class StompEventUpdateWorkoutRequestMessageDepth1(
    val memberId : Long,
    val message : StompEventUpdateWorkoutRequestMessageDepth2,
)