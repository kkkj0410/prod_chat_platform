package kr.co.fitview.api.app.domain.invitation.controller

import jakarta.validation.Valid
import kr.co.fitview.api.app.domain.invitation.dto.request.InvitationWorkoutPartnerRequest
import kr.co.fitview.api.app.domain.invitation.dto.response.InvitationMeResponse
import kr.co.fitview.api.app.domain.invitation.service.InvitationCodeProvider
import kr.co.fitview.api.app.domain.invitation.service.InvitationService
import kr.co.fitview.api.app.global.dto.ApiResponse
import kr.co.fitview.api.app.global.util.SecurityUtil
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/v1/invitations")
class InvitationController(
    private val securityUtil : SecurityUtil,
    private val invitationCodeProvider: InvitationCodeProvider,
    private val invitationService: InvitationService
) {


    @GetMapping("/me")
    fun invitationDetails(): ResponseEntity<ApiResponse<InvitationMeResponse>> {

        return ResponseEntity.ok(ApiResponse.success(
            InvitationMeResponse(
                invitationCode = invitationCodeProvider.encode(securityUtil.getMemberId())
            )
        ))

    }

    @PostMapping("/workout-partners")
    fun invitationWorkoutPartnerAdd(
        @Valid
        @RequestBody
        request : InvitationWorkoutPartnerRequest
    ) : ResponseEntity<ApiResponse<*>> {

        invitationService.processWorkoutPartner(
            memberId = securityUtil.getMemberId(),
            request = request.toServiceRequest()
        )

        return ResponseEntity.ok(ApiResponse.success("ok"))
    }



}