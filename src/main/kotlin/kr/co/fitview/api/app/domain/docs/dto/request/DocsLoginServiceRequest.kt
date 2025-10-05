package kr.co.fitview.api.app.domain.docs.dto.request

import jakarta.validation.constraints.NotBlank
import kr.co.fitview.api.app.domain.member.dto.request.MemberLoginServiceRequest

data class DocsLoginServiceRequest(
    val loginId : String,
    val password : String

)
