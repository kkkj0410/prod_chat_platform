package kr.co.fitview.api.app.domain.oauth2.dto.request

import jakarta.validation.constraints.NotBlank
import kr.co.fitview.api.app.domain.auth.dto.request.AccessTokenRefreshServiceRequest

data class AppleLoginRequest(

    @field:NotBlank(message = "appleAuthCode is required")
    val appleAuthCode : String?,

    @field:NotBlank(message = "redirectUri is required")
    val redirectUri : String?,

){
    fun toServiceRequest() : AppleLoginServiceRequest{
        return AppleLoginServiceRequest(
            appleAuthCode = appleAuthCode!!,
            redirectUri = redirectUri!!
        )
    }

}
