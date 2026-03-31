package kr.co.fitview.api.app.domain.member.dto.response

import jakarta.validation.constraints.NotBlank
import kr.co.fitview.api.app.domain.member.dto.request.MemberReserveNicknameServiceRequest

data class MemberReserveNicknameResponse(

    val isReserved : Boolean

)