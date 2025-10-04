package kr.co.fitview.api.app.domain.auth.dto.request

import jakarta.validation.constraints.NotBlank
import kr.co.fitview.api.app.domain.member.dto.request.MemberCreateServiceRequest

data class MemberCreateRequest(

    @field:NotBlank(message = "Email is required")
    val email : String?,

    @field:NotBlank(message = "Password is required")
    val password : String?


){
    fun toServiceRequest() : MemberCreateServiceRequest {
        return MemberCreateServiceRequest(
            email = email!!,
            password = password!!
        )
    }
}
