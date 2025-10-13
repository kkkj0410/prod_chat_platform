package kr.co.fitview.api.app.domain.oauth2.dto.request

import jakarta.validation.constraints.NotBlank

data class KakaoLoginRequest(

    @field:NotBlank(message = "kakaoAccessToken is required")
    val kakaoAccessToken : String?,

    ){
    fun toServiceRequest() : KakaoLoginServiceRequest{
        return KakaoLoginServiceRequest(
            kakaoAccessToken = kakaoAccessToken!!
        )
    }

}
