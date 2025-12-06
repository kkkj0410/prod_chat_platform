package kr.co.fitview.api.app.domain.notification.dto.response

data class StompEventWorkoutRequestMessageDepth1 (
    val memberId : Long,
    val message : StompEventWorkoutRequestMessageDepth2
)