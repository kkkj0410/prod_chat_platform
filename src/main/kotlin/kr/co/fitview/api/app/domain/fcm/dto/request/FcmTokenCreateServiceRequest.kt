package kr.co.fitview.api.app.domain.fcm.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import kr.co.fitview.api.app.domain.chat.dto.request.ChatWorkoutRequestMessageServiceRequest
import kr.co.fitview.api.app.domain.fcm.entity.enums.FcmTokenPlatform

data class FcmTokenCreateServiceRequest(
    val deviceId : String,
    val token : String,
    val platform : FcmTokenPlatform
)