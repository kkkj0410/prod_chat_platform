package kr.co.fitview.api.app.domain.fcm.dto.response

import kr.co.fitview.api.app.domain.fcm.entity.enums.FcmTokenPlatform
import java.time.LocalDateTime

data class FcmTokenActiveResponse(
    val email : String,
    val nickname : String,
    val fcmTokenId : Long,
    val memberId : Long,
    val createdAt : LocalDateTime,
    val updatedAt : LocalDateTime,
    val deviceId : String,
    val fcmToken : String,
    val platform : FcmTokenPlatform,
)