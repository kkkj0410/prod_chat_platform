package kr.co.fitview.api.app.domain.auth.dto.request

import jakarta.validation.constraints.NotBlank
import kr.co.fitview.api.app.domain.member.dto.request.MemberLoginServiceRequest

data class MemberLogoutRequest(

    @field:NotBlank(message = "RefreshToken is required")
    val refreshToken : String?

){

}
