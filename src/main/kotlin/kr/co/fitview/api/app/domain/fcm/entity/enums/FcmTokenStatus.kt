package kr.co.fitview.api.app.domain.fcm.entity.enums

import kr.co.fitview.api.app.domain.image.enums.S3Prefix
import kr.co.fitview.api.app.domain.member.entity.enums.WorkoutTimeName

enum class FcmTokenStatus(
    val description : String
) {
    ACTIVE("활성화"),
    INVALID("FCM 토큰이 유효하지 않음"),
    REVOKED("FCM 토큰의 유효성을 회수(토큰 자체는 유효한 상태). 동일한 FCM 토큰을 다른 device에 사용 시, 기존 데이터는 REVOKED")

    ;



}