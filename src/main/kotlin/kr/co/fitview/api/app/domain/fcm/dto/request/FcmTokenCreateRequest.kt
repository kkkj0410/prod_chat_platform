package kr.co.fitview.api.app.domain.fcm.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import kr.co.fitview.api.app.domain.chat.dto.request.ChatWorkoutRequestMessageServiceRequest
import kr.co.fitview.api.app.domain.fcm.entity.enums.FcmTokenPlatform

data class FcmTokenCreateRequest(

    @field:NotBlank(message = "deviceId is required")
    val deviceId : String?,

    @field:NotBlank(message = "token is required")
    val token : String?,

    @field:NotNull(message = "platform is required")
    val platform : FcmTokenPlatform?
){

    fun toServiceRequest(): FcmTokenCreateServiceRequest {
        return FcmTokenCreateServiceRequest(
            deviceId = deviceId!!,
            token = token!!,
            platform = platform!!
        )
    }
}