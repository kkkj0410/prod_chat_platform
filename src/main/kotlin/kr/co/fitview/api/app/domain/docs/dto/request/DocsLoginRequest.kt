package kr.co.fitview.api.app.domain.docs.dto.request

import jakarta.validation.constraints.NotBlank

data class DocsLoginRequest(

    @field:NotBlank(message = "Email is required")
    val email : String?,

    @field:NotBlank(message = "Password is required")
    val password : String?

){

    fun toServiceRequest() : DocsLoginServiceRequest {
        return DocsLoginServiceRequest(
            email = email!!,
            password = password!!
        )
    }
}
