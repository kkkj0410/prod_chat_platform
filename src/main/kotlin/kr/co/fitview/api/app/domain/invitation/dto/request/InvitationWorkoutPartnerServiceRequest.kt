package kr.co.fitview.api.app.domain.invitation.dto.request

import jakarta.validation.constraints.NotBlank
import kr.co.fitview.api.app.domain.member.dto.request.MemberReserveNicknameServiceRequest

data class InvitationWorkoutPartnerServiceRequest(

    val invitationCode : String
)
