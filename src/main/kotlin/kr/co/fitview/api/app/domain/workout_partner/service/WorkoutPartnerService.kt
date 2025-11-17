package kr.co.fitview.api.app.domain.workout_partner.service

import kr.co.fitview.api.app.domain.member.service.MemberService
import kr.co.fitview.api.app.domain.workout_partner.dto.request.WorkoutPartnerCreateServiceRequest
import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartner
import kr.co.fitview.api.app.domain.workout_partner.repository.WorkoutPartnerRepository
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.workout_partner.WorkoutPartnerErrorCode
import kr.co.fitview.api.app.global.time.Time
import org.springframework.stereotype.Service


@Service
class WorkoutPartnerService(
    private val workoutPartnerRepository: WorkoutPartnerRepository,
    private val memberService : MemberService,
    private val time : Time
) {


    fun addWorkoutPartner(memberId : Long, request: WorkoutPartnerCreateServiceRequest): WorkoutPartner {
        val findWorkoutPartner = workoutPartnerRepository.findTop1ByFromMemberIdAndToMemberIdAndDeletedAtIsNullOrderByRequestedAtDesc(memberId, request.memberId)

        validateAddWorkoutPartner(findWorkoutPartner)

        val workoutPartner = WorkoutPartner(
            memberService.findMemberReferenceFrom(memberId),
            memberService.findMemberReferenceFrom(request.memberId),
            time.nowLocalDateTime
        )
        return workoutPartnerRepository.save(workoutPartner)
    }

    private fun validateAddWorkoutPartner(workoutPartner: WorkoutPartner?) {
        if (isNotNull(workoutPartner)) {
            if (isNotNull(workoutPartner!!.acceptedAt)) {
                throw GlobalException(WorkoutPartnerErrorCode.ALREADY_PARTNER_ACCEPTED)
            }

            if (isNotExpire24Hour(workoutPartner) && isPending(workoutPartner)) {
                throw GlobalException(WorkoutPartnerErrorCode.PARTNER_REQUEST_COOLDOWN)
            }
        }
    }

    private fun isNotExpire24Hour(workoutPartner: WorkoutPartner) =
        workoutPartner.requestedAt!!.isAfter(time.nowLocalDateTime.minusHours(24))

    private fun isPending(workoutPartner : WorkoutPartner) =
        isNull(workoutPartner.rejectedAt) && isNull(workoutPartner.canceledAt)

    private fun isNotNull(value: Any?) =
        value != null

    private fun isNull(value: Any?) =
        value == null


}