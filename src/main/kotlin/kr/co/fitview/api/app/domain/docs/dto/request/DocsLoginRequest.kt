package kr.co.fitview.api.app.domain.docs.dto.request

import jakarta.validation.constraints.NotBlank
import kr.co.fitview.api.app.domain.member.dto.request.MemberLoginServiceRequest

data class DocsLoginRequest(

    @field:NotBlank(message = "LoginId is required")
    val loginId : String?,

    @field:NotBlank(message = "Password is required")
    val password : String?

){

    fun toServiceRequest() : DocsLoginServiceRequest {
        return DocsLoginServiceRequest(
            loginId = loginId!!,
            password = password!!
        )
    }
}
