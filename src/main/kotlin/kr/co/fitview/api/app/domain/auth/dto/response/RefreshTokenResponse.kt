package kr.co.fitview.api.app.domain.auth.dto.response

data class RefreshTokenResponse(
    val refreshTokenUid : String,
    val deviceId : String
)