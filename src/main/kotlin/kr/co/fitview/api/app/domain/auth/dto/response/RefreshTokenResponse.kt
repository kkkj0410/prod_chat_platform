package kr.co.fitview.api.app.domain.auth.dto.response

data class RefreshTokenResponse(
    val refreshTokenId : String,
    val deviceId : String
)