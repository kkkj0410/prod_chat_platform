package kr.co.fitview.api.app.domain.workout_partner.service

import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.global.stomp.dto.request.StompEventAcceptWorkoutPartnerDepth1
import kr.co.fitview.api.app.global.stomp.dto.request.StompEventAcceptWorkoutPartnerDepth2
import kr.co.fitview.api.app.global.stomp.dto.request.StompEventWorkoutPartnerRequestDepth1
import kr.co.fitview.api.app.global.stomp.dto.request.StompEventWorkoutPartnerRequestDepth2
import kr.co.fitview.api.app.domain.workout_partner.dto.request.WorkoutPartnerCreateServiceRequest
import kr.co.fitview.api.app.domain.workout_partner.dto.request.WorkoutPartnerUpdateServiceRequest
import kr.co.fitview.api.app.domain.workout_partner.dto.request.enums.WorkoutPartnerRequestUpdateStatus
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
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class WorkoutPartnerRequestService(
    private val workoutPartnerRepository: WorkoutPartnerRepository,
    private val workoutPartnerRequestRepository : WorkoutPartnerRequestRepository,
    private val memberQueryService : MemberQueryService,
    private val publisher: ApplicationEventPublisher,
    private val time : Time
) {


    @Transactional
    fun addWorkoutPartnerRequest(memberId : Long, request: WorkoutPartnerCreateServiceRequest): WorkoutPartnerRequest {
        validateAlreadyWorkoutPartner(memberId, request.memberId)


        // FE 편의상 validate 취소
        // 정식상으로 다시 validate 활성화 필요
//        val findWorkoutPartner = workoutPartnerRequestRepository.findTop1ByFromMemberIdAndToMemberIdAndDeletedAtIsNullOrderByRequestedAtDesc(memberId, request.memberId)

//        validateAddWorkoutPartnerRequest(findWorkoutPartner)

        val workoutPartnerRequest = WorkoutPartnerRequest.of(
            fromMember = memberQueryService.findMemberReferenceFrom(memberId),
            toMember = memberQueryService.findMemberReferenceFrom(request.memberId),
            now = time.nowLocalDateTime,
            content = request.workoutPartnerRequestContentIndex
        )

        val savedWorkoutPartnerRequest = workoutPartnerRequestRepository.save(workoutPartnerRequest)

        sendStompWorkoutPartnerRequest(savedWorkoutPartnerRequest)

        return savedWorkoutPartnerRequest
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

            sendStompAcceptWorkoutPartnerRequest(findWorkoutPartnerRequest)

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

    private fun sendStompWorkoutPartnerRequest(
        workoutPartnerRequest: WorkoutPartnerRequest
    ) {
        val findMemberProfile =
            memberQueryService.findMemberWorkoutRequestProfileFrom(workoutPartnerRequest.getFromMemberId())
                ?: throw GlobalException(GlobalErrorCode.ENTITY_NOT_FOUND)

        val stomp = StompEventWorkoutPartnerRequestDepth1(
            memberId = workoutPartnerRequest.getToMemberId(),
            message = StompEventWorkoutPartnerRequestDepth2(
                workoutPartnerRequestId = workoutPartnerRequest.id!!,
                memberId = findMemberProfile.memberId,
                profileImageUrl = findMemberProfile.profileImageUrl,
                nickname = findMemberProfile.nickname
            )
        )
        publisher.publishEvent(stomp)
    }

    private fun sendStompAcceptWorkoutPartnerRequest(
        workoutPartnerRequest: WorkoutPartnerRequest,
    ) {
        val findMemberProfile1 =
            memberQueryService.findMemberWorkoutRequestProfileFrom(workoutPartnerRequest.getToMemberId())
                ?: throw GlobalException(GlobalErrorCode.ENTITY_NOT_FOUND)

        val stomp1 = StompEventAcceptWorkoutPartnerDepth1(
            memberId = workoutPartnerRequest.getFromMemberId(),
            message = StompEventAcceptWorkoutPartnerDepth2(
                memberId = findMemberProfile1.memberId,
                profileImageUrl = findMemberProfile1.profileImageUrl,
                nickname = findMemberProfile1.nickname,
                workoutPartnerRequestContentIndex = workoutPartnerRequest.content!!.index
            )
        )

        val findMemberProfile2 =
            memberQueryService.findMemberWorkoutRequestProfileFrom(workoutPartnerRequest.getFromMemberId())
                ?: throw GlobalException(GlobalErrorCode.ENTITY_NOT_FOUND)

        val stomp2 = StompEventAcceptWorkoutPartnerDepth1(
            memberId = workoutPartnerRequest.getToMemberId(),
            message = StompEventAcceptWorkoutPartnerDepth2(
                memberId = findMemberProfile2.memberId,
                profileImageUrl = findMemberProfile2.profileImageUrl,
                nickname = findMemberProfile2.nickname,
                workoutPartnerRequestContentIndex = workoutPartnerRequest.content!!.index
            )
        )

        publisher.publishEvent(stomp1)
        publisher.publishEvent(stomp2)
    }

    private fun validateUpdateWorkoutPartnerRequest(workoutPartnerRequest: WorkoutPartnerRequest) {
        if (isNotPending(workoutPartnerRequest) || isExpire24Hour(workoutPartnerRequest))
        {
            throw GlobalException(WorkoutPartnerErrorCode.PARTNER_REQUEST_ALREADY_FINALIZED)
        }
    }

    fun isExpire24Hour(workoutPartnerRequest: WorkoutPartnerRequest) =
        workoutPartnerRequest.requestedAt!!.isBefore(time.nowLocalDateTime.minusHours(24))


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

    fun isNotExpire24Hour(workoutPartnerRequest: WorkoutPartnerRequest) =
        workoutPartnerRequest.requestedAt!!.isAfter(time.nowLocalDateTime.minusHours(24))


}