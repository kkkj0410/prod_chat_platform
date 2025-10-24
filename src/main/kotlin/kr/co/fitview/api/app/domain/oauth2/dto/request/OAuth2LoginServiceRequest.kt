package kr.co.fitview.api.app.domain.oauth2.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import kr.co.fitview.api.app.domain.auth.dto.request.AccessTokenRefreshServiceRequest
import kr.co.fitview.api.app.global.entity.OAuth2Provider

data class OAuth2LoginServiceRequest(

    val provider : OAuth2Provider,
    val providerToken : String,

    ){

    fun toAppleServiceRequest() : AppleLoginServiceRequest{
        return AppleLoginServiceRequest(
            appleAuthCode = providerToken
        )
    }

    fun toKakaoServiceRequest() : KakaoLoginServiceRequest{
        return KakaoLoginServiceRequest(
            kakaoAccessToken = providerToken
        )
    }

    fun toGoogleServiceRequest(): GoogleLoginServiceRequest {
       return GoogleLoginServiceRequest(
           googleAuthCode = providerToken
       )
    }

}
