package kr.co.fitview.api.app.domain.fcm.dto.request

import kr.co.fitview.api.app.domain.fcm.entity.enums.FcmTokenPlatform

data class FcmTokenCreateRequest(
    val deviceId : String,
    val token : String,
    val platform : FcmTokenPlatform
)