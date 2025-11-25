package kr.co.fitview.api.app.domain.workout_partner.service

import kr.co.fitview.api.app.domain.member.service.MemberReferenceProvider
import kr.co.fitview.api.app.domain.notification.service.NotificationStompService
import kr.co.fitview.api.app.domain.workout_partner.condition.WorkoutPartnerRequestCondition
import kr.co.fitview.api.app.domain.workout_partner.dto.request.WorkoutPartnerCreateServiceRequest
import kr.co.fitview.api.app.domain.workout_partner.dto.request.WorkoutPartnerUpdateServiceRequest
import kr.co.fitview.api.app.domain.workout_partner.dto.request.enums.WorkoutPartnerRequestUpdateStatus
import kr.co.fitview.api.app.domain.workout_partner.dto.response.WorkoutPartnerRequestResponse
import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartner
import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartnerRequest
import kr.co.fitview.api.app.domain.workout_partner.entity.enums.WorkoutPartnerRequestStatus
import kr.co.fitview.api.app.domain.workout_partner.repository.WorkoutPartnerRepository
import kr.co.fitview.api.app.domain.workout_partner.repository.WorkoutPartnerRequestRepository
import kr.co.fitview.api.app.domain.workout_partner.repository.findByOrderedMemberOneIdAndMemberTwoIdAndDeletedAtIsNull
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.global.GlobalErrorCode
import kr.co.fitview.api.app.global.exception.error.workout_partner.WorkoutPartnerErrorCode
import kr.co.fitview.api.app.global.time.Time
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class WorkoutPartnerRequestService(
    private val workoutPartnerRepository: WorkoutPartnerRepository,
    private val workoutPartnerRequestRepository : WorkoutPartnerRequestRepository,
    private val memberReferenceProvider : MemberReferenceProvider,
    private val notificationStompService: NotificationStompService,
    private val time : Time
) {


    @Transactional
    fun addWorkoutPartnerRequest(memberId : Long, request: WorkoutPartnerCreateServiceRequest): WorkoutPartnerRequest {
        validateAlreadyWorkoutPartner(memberId, request.memberId)

        val findWorkoutPartner = workoutPartnerRequestRepository.findTop1ByFromMemberIdAndToMemberIdAndDeletedAtIsNullOrderByRequestedAtDesc(memberId, request.memberId)

        // FE 편의상 validate 취소
        // 정식상으로 다시 validate 활성화 필요
//        validateAddWorkoutPartnerRequest(findWorkoutPartner)

        val workoutPartner = WorkoutPartnerRequest(
            memberReferenceProvider.findMemberReferenceFrom(memberId),
            memberReferenceProvider.findMemberReferenceFrom(request.memberId),
            WorkoutPartnerRequestStatus.PENDING,
            time.nowLocalDateTime
        )

        val savedWorkoutPartner = workoutPartnerRequestRepository.save(workoutPartner)


        return savedWorkoutPartner
    }

    @Transactional
    fun updateWorkoutPartnerRequest(memberId: Long, workoutPartnerRequestId : Long, request: WorkoutPartnerUpdateServiceRequest) : WorkoutPartnerRequest {
        val findWorkoutPartnerRequest = workoutPartnerRequestRepository.findByIdAndToMemberIdAndDeletedAtIsNull(workoutPartnerRequestId, memberId)
                ?: throw GlobalException(GlobalErrorCode.ENTITY_NOT_FOUND)

        validateUpdateWorkoutPartnerRequest(findWorkoutPartnerRequest)

        validateAlreadyWorkoutPartner(findWorkoutPartnerRequest.getFromMemberId(), findWorkoutPartnerRequest.getToMemberId())

        if(isWorkoutPartnerAccept(request)){
            findWorkoutPartnerRequest.accept()

            addWorkoutPartner(findWorkoutPartnerRequest)

            return findWorkoutPartnerRequest
        }

        findWorkoutPartnerRequest.reject()
        return findWorkoutPartnerRequest
    }

    @Transactional
    fun addWorkoutPartner(workoutPartnerRequest: WorkoutPartnerRequest) {
        val workoutPartner =
            WorkoutPartner.of(workoutPartnerRequest.fromMember!!, workoutPartnerRequest.toMember!!)
        workoutPartnerRepository.save(workoutPartner)
    }

    fun findRecentRequestWithin24Hours(fromMemberId : Long, toMemberId : Long) : WorkoutPartnerRequest?{
        val findWorkoutPartnerRequest = workoutPartnerRequestRepository.findTop1ByFromMemberIdAndToMemberIdAndDeletedAtIsNullOrderByRequestedAtDesc(fromMemberId, toMemberId)

        if(isNotNull(findWorkoutPartnerRequest) && isNotExpire24Hour(findWorkoutPartnerRequest!!)){
            return findWorkoutPartnerRequest
        }

        return null
    }

    fun findWorkoutPartnerFrom(memberId: Long, condition: WorkoutPartnerRequestCondition) : Slice<WorkoutPartnerRequestResponse> {
        return workoutPartnerRequestRepository.findWorkoutPartnerByConditionAndDeletedAtIsNull(memberId, condition)
    }


    private fun validateAddWorkoutPartnerRequest(workoutPartnerRequest: WorkoutPartnerRequest?) {
        if (isNotNull(workoutPartnerRequest)) {
            if (workoutPartnerRequest!!.status == WorkoutPartnerRequestStatus.ACCEPT) {
                throw GlobalException(WorkoutPartnerErrorCode.ALREADY_PARTNER_ACCEPTED)
            }

            if (isNotExpire24Hour(workoutPartnerRequest) && isPending(workoutPartnerRequest)) {
                throw GlobalException(WorkoutPartnerErrorCode.PARTNER_REQUEST_COOLDOWN)
            }
        }
    }

    private fun validateUpdateWorkoutPartnerRequest(workoutPartnerRequest: WorkoutPartnerRequest) {
        if (isNotPending(workoutPartnerRequest) || isExpire24Hour(workoutPartnerRequest))
        {
            throw GlobalException(WorkoutPartnerErrorCode.PARTNER_REQUEST_ALREADY_FINALIZED)
        }
    }

    fun isExpire24Hour(workoutPartnerRequest: WorkoutPartnerRequest) =
        workoutPartnerRequest.requestedAt!!.isBefore(time.nowLocalDateTime.minusHours(24))

    fun isNotExpire24Hour(workoutPartnerRequest: WorkoutPartnerRequest) =
        workoutPartnerRequest.requestedAt!!.isAfter(time.nowLocalDateTime.minusHours(24))

    private fun isNotPending(workoutPartnerRequest: WorkoutPartnerRequest) =
        workoutPartnerRequest.status != WorkoutPartnerRequestStatus.PENDING

    private fun isWorkoutPartnerAccept(request: WorkoutPartnerUpdateServiceRequest) =
        request.type == WorkoutPartnerRequestUpdateStatus.ACCEPT

    private fun isPending(workoutPartnerRequest : WorkoutPartnerRequest) =
        workoutPartnerRequest.status == WorkoutPartnerRequestStatus.PENDING

    private fun isNotNull(value: Any?) =
        value != null


    private fun validateAlreadyWorkoutPartner(
        memberOneId: Long,
        memberTwoId: Long
    ) {
        workoutPartnerRepository
            .findByOrderedMemberOneIdAndMemberTwoIdAndDeletedAtIsNull(memberOneId, memberTwoId)
            ?.let { throw GlobalException(WorkoutPartnerErrorCode.ALREADY_PARTNER_ACCEPTED) }
    }



}