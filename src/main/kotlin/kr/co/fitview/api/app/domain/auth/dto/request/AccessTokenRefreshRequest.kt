package kr.co.fitview.api.app.domain.auth.dto.request

import jakarta.validation.constraints.NotBlank

data class AccessTokenRefreshRequest(

    @field:NotBlank(message = "RefreshToken is required")
    val refreshToken : String?,

){
    fun toServiceRequest() : AccessTokenRefreshServiceRequest {
        return AccessTokenRefreshServiceRequest(
            refreshToken = refreshToken!!
        )
    }
}
