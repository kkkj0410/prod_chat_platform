package kr.co.fitview.api.app.domain.member.dto.request

import jakarta.validation.constraints.NotBlank

data class MemberReserveNicknameRequest(

    @field:NotBlank(message = "nickname is required")
    val nickname : String?

){

    fun toServiceRequest() : MemberReserveNicknameServiceRequest{
        return MemberReserveNicknameServiceRequest(
            nickname = nickname!!
        )

    }

}
