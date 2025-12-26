package kr.co.fitview.api.app.domain.oauth2.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import kr.co.fitview.api.app.domain.auth.dto.request.AccessTokenRefreshServiceRequest
import kr.co.fitview.api.app.global.entity.OAuth2Provider

data class OAuth2LoginRequest(

    @field:NotNull(message = "provider is required")
    val provider : OAuth2Provider?,

    @field:NotBlank(message = "providerToken is required")
    val providerToken : String?,

    @field:NotBlank(message = "deviceId is required")
    val deviceId : String?

    ){
    fun toServiceRequest() : OAuth2LoginServiceRequest{
        return OAuth2LoginServiceRequest(
            provider = provider!!,
            providerToken = providerToken!!,
            deviceId = deviceId!!
        )
    }
}
