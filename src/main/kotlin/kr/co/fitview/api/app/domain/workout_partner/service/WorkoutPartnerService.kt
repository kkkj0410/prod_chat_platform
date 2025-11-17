package kr.co.fitview.api.app.domain.workout_partner.service

import kr.co.fitview.api.app.domain.member.service.MemberService
import kr.co.fitview.api.app.domain.workout_partner.dto.request.WorkoutPartnerCreateServiceRequest
import kr.co.fitview.api.app.domain.workout_partner.dto.request.WorkoutPartnerUpdateServiceRequest
import kr.co.fitview.api.app.domain.workout_partner.dto.request.enums.WorkoutPartnerUpdateType
import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartner
import kr.co.fitview.api.app.domain.workout_partner.repository.WorkoutPartnerRepository
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.global.GlobalErrorCode
import kr.co.fitview.api.app.global.exception.error.workout_partner.WorkoutPartnerErrorCode
import kr.co.fitview.api.app.global.time.Time
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class WorkoutPartnerService(
    private val workoutPartnerRepository: WorkoutPartnerRepository,
    private val memberService : MemberService,
    private val time : Time
) {


    @Transactional
    fun addWorkoutPartner(memberId : Long, request: WorkoutPartnerCreateServiceRequest): WorkoutPartner {
        val findWorkoutPartner = workoutPartnerRepository.findTop1ByFromMemberIdAndToMemberIdAndDeletedAtIsNullOrderByRequestedAtDesc(memberId, request.memberId)

        // FE 편의상 validate 취소
        // 정식상으로 다시 validate 활성화 필요
//        validateAddWorkoutPartner(findWorkoutPartner)

        val workoutPartner = WorkoutPartner(
            memberService.findMemberReferenceFrom(memberId),
            memberService.findMemberReferenceFrom(request.memberId),
            time.nowLocalDateTime
        )
        return workoutPartnerRepository.save(workoutPartner)
    }

    @Transactional
    fun updateWorkoutPartner(memberId: Long, workoutPartnerId : Long, request: WorkoutPartnerUpdateServiceRequest) : WorkoutPartner {
        val findWorkoutPartner = workoutPartnerRepository.findByIdAndToMemberIdAndDeletedAtIsNull(workoutPartnerId, memberId)
                ?: throw GlobalException(GlobalErrorCode.ENTITY_NOT_FOUND)

        validateUpdateWorkoutPartner(findWorkoutPartner)

        if(isWorkoutPartnerAccept(request)){
            findWorkoutPartner.accept(time.nowLocalDateTime)
        }

        else if(isWorkoutPartnerReject(request)){
            findWorkoutPartner.reject(time.nowLocalDateTime)
        }

        return findWorkoutPartner
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

    private fun validateUpdateWorkoutPartner(findWorkoutPartner: WorkoutPartner) {
        if (isNotNull(findWorkoutPartner.canceledAt) || isNotNull(findWorkoutPartner.rejectedAt) || isNotNull(
                findWorkoutPartner.acceptedAt
            )
        ) {
            throw GlobalException(WorkoutPartnerErrorCode.PARTNER_REQUEST_ALREADY_FINALIZED)
        }
    }

    private fun isWorkoutPartnerAccept(request: WorkoutPartnerUpdateServiceRequest) =
        request.type == WorkoutPartnerUpdateType.ACCEPT

    private fun isWorkoutPartnerReject(request: WorkoutPartnerUpdateServiceRequest) =
        request.type == WorkoutPartnerUpdateType.REJECT

    private fun isNotExpire24Hour(workoutPartner: WorkoutPartner) =
        workoutPartner.requestedAt!!.isAfter(time.nowLocalDateTime.minusHours(24))

    private fun isPending(workoutPartner : WorkoutPartner) =
        isNull(workoutPartner.rejectedAt) && isNull(workoutPartner.canceledAt)

    private fun isNotNull(value: Any?) =
        value != null

    private fun isNull(value: Any?) =
        value == null



}