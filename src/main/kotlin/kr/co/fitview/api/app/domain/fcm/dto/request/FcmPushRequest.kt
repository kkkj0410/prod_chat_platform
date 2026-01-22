package kr.co.fitview.api.app.domain.fcm.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import kr.co.fitview.api.app.domain.fcm.entity.enums.FcmTokenPlatform

data class FcmPushRequest(

    @field:NotBlank(message = "fcmToken is required")
    val fcmToken : String?,

    @field:NotNull(message = "platform is required")
    val platform : FcmTokenPlatform?,

    val title : String = "title",

    val body : String = "body",
)
