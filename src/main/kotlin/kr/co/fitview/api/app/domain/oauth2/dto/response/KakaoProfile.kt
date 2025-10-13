package kr.co.fitview.api.app.domain.oauth2.dto.response

data class KakaoProfile(
    val id : String,
    val kakao_account : KakaoAccount
)

data class KakaoAccount(
    val email : String
)