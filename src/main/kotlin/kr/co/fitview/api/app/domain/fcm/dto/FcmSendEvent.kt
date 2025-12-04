package kr.co.fitview.api.app.domain.fcm.dto

import kr.co.fitview.api.app.domain.fcm.entity.enums.FcmTokenPlatform

data class FcmSendEvent(
    val token : String,
    val title : String,
    val body : String,
    val platform : FcmTokenPlatform
)