package kr.co.fitview.api.app.domain.invitation.service

import kr.co.fitview.api.app.domain.invitation.dto.request.InvitationWorkoutPartnerServiceRequest
import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartner
import kr.co.fitview.api.app.domain.workout_partner.service.WorkoutPartnerRequestService
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.invitation.InvitationErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional
class InvitationService(
    private val invitationCodeProvider : InvitationCodeProvider,
    private val workoutPartnerRequestService: WorkoutPartnerRequestService,
    private val memberQueryService: MemberQueryService,
) {

    fun processWorkoutPartner(memberId: Long, request: InvitationWorkoutPartnerServiceRequest): WorkoutPartner {

        val toMemberId = invitationCodeProvider.decode(request.invitationCode)

        memberQueryService.findMemberFromIdAndSignup(toMemberId)
            ?: throw GlobalException(InvitationErrorCode.INVITATION_MEMBER_NOT_FOUND)

        return workoutPartnerRequestService.addWorkoutPartnerDirectly(
            fromMemberId = memberId,
            toMemberId = toMemberId
        )

    }


}