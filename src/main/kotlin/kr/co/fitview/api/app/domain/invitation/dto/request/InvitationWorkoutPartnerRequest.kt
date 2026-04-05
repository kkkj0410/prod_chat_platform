package kr.co.fitview.api.app.domain.invitation.dto.request

import jakarta.validation.constraints.NotBlank
import kr.co.fitview.api.app.domain.member.dto.request.MemberReserveNicknameServiceRequest

data class InvitationWorkoutPartnerRequest(

    @field:NotBlank(message = "invitationCode is required")
    val invitationCode : String?
){

    fun toServiceRequest() : InvitationWorkoutPartnerServiceRequest {
        return InvitationWorkoutPartnerServiceRequest(
            invitationCode = invitationCode!!
        )
    }

}
