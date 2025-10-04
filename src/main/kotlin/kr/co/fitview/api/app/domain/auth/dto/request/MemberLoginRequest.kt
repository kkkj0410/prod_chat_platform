package kr.co.fitview.api.app.domain.auth.dto.request

import jakarta.validation.constraints.NotBlank
import kr.co.fitview.api.app.domain.member.dto.request.MemberLoginServiceRequest

data class MemberLoginRequest(

    @field:NotBlank(message = "LoginId is required")
    val loginId : String?,

    @field:NotBlank(message = "Password is required")
    val password : String?

){
    fun toServiceRequest() : MemberLoginServiceRequest{
        return MemberLoginServiceRequest(
            loginId = loginId!!,
            password = password!!
        )
    }
}
