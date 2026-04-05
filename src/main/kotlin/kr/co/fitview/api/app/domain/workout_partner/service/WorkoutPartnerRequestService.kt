package kr.co.fitview.api.app.domain.workout_partner.service

import kr.co.fitview.api.app.domain.fcm.dto.request.EventFcmWorkoutPartnerAccept
import kr.co.fitview.api.app.domain.fcm.dto.request.EventFcmWorkoutPartnerRequest
import kr.co.fitview.api.app.domain.member.dto.response.MemberProfile
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.domain.notification.dto.request.*
import kr.co.fitview.api.app.global.stomp.dto.request.StompEventAcceptWorkoutPartnerDepth1
import kr.co.fitview.api.app.global.stomp.dto.request.StompEventAcceptWorkoutPartnerDepth2
import kr.co.fitview.api.app.global.stomp.dto.request.StompEventWorkoutPartnerRequestDepth1
import kr.co.fitview.api.app.global.stomp.dto.request.StompEventWorkoutPartnerRequestDepth2
import kr.co.fitview.api.app.domain.workout_partner.dto.request.WorkoutPartnerCreateServiceRequest
import kr.co.fitview.api.app.domain.workout_partner.dto.request.WorkoutPartnerUpdateServiceRequest
import kr.co.fitview.api.app.domain.workout_partner.dto.request.enums.WorkoutPartnerRequestUpdateStatus
import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartner
import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartnerRequest
import kr.co.fitview.api.app.domain.workout_partner.entity.enums.WorkoutPartnerRequestContent
import kr.co.fitview.api.app.domain.workout_partner.entity.enums.WorkoutPartnerRequestStatus
import kr.co.fitview.api.app.domain.workout_partner.repository.WorkoutPartnerRepository
import kr.co.fitview.api.app.domain.workout_partner.repository.WorkoutPartnerRequestRepository
import kr.co.fitview.api.app.domain.workout_partner.repository.findByOrderedMemberOneIdAndMemberTwoIdAndDeletedAtIsNull
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.global.GlobalErrorCode
import kr.co.fitview.api.app.global.exception.error.member.MemberErrorCode
import kr.co.fitview.api.app.global.exception.error.workout_partner.WorkoutPartnerErrorCode
import kr.co.fitview.api.app.global.random.RandomCustom
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
    private val time : Time,
    private val randomCustom : RandomCustom

) {

    @Transactional
    fun addWorkoutPartnerDirectly(
        fromMemberId : Long,
        toMemberId : Long,
        seed: Long = System.currentTimeMillis()
    ) : WorkoutPartner{

        validateNotSelfWorkoutPartnerRequest(fromMemberId, toMemberId)

        validateAlreadyWorkoutPartner(fromMemberId, toMemberId)

        val fromMember = memberQueryService.findMemberFromId(fromMemberId)
            ?: throw GlobalException(MemberErrorCode.MEMBER_NOT_FOUND)

        val toMember = memberQueryService.findMemberReferenceFrom(toMemberId)

        val workoutPartnerRequest = WorkoutPartnerRequest.of(
            fromMember = fromMember,
            toMember = toMember,
            now = time.nowLocalDateTime,
            content = getRandomPartnerContentByShuffle(
                seed = seed
            )
        )
        workoutPartnerRequest.accept()

        val savedWorkoutPartnerRequest =  workoutPartnerRequestRepository.save(workoutPartnerRequest)

        val savedWorkoutPartner = addWorkoutPartner(savedWorkoutPartnerRequest)

        sendStompAcceptWorkoutPartnerRequest(savedWorkoutPartnerRequest)

        sendFcmNotificationAcceptWorkoutPartnerRequest(savedWorkoutPartnerRequest.getFromMemberId(), savedWorkoutPartnerRequest.getToMemberId())

        sendNotificationAcceptWorkoutPartnerRequest(savedWorkoutPartnerRequest, savedWorkoutPartner.id!!)

        return savedWorkoutPartner
    }

    fun getRandomPartnerContentByShuffle(seed: Long): WorkoutPartnerRequestContent {
        val entries = WorkoutPartnerRequestContent.entries
        return randomCustom.shuffled(seed, entries).first()
    }

    @Transactional
    fun addWorkoutPartnerRequest(memberId : Long, request: WorkoutPartnerCreateServiceRequest): WorkoutPartnerRequest {

        validateNotSelfWorkoutPartnerRequest(memberId, request.memberId)

        validateAlreadyWorkoutPartner(memberId, request.memberId)

        val findWorkoutPartner = workoutPartnerRequestRepository.findTop1ByFromMemberIdAndToMemberIdOrderByRequestedAtDesc(memberId, request.memberId)

        validateAddWorkoutPartnerRequest(findWorkoutPartner)

        val toMember = memberQueryService.findMemberReferenceFrom(request.memberId)
        val fromMember = memberQueryService.findMemberFromId(memberId)
            ?: throw GlobalException(MemberErrorCode.MEMBER_NOT_FOUND)

        val workoutPartnerRequest = WorkoutPartnerRequest.of(
            fromMember = fromMember,
            toMember = toMember,
            now = time.nowLocalDateTime,
            content = request.workoutPartnerRequestContentIndex
        )

        val savedWorkoutPartnerRequest = workoutPartnerRequestRepository.save(workoutPartnerRequest)

        sendStompWorkoutPartnerRequest(savedWorkoutPartnerRequest)

        sendFcmWorkoutPartnerRequest(toMember, fromMember)

        sendNotificationWorkoutPartnerRequest(memberId, request.memberId, savedWorkoutPartnerRequest.id!!)

        return savedWorkoutPartnerRequest
    }

    private fun validateNotSelfWorkoutPartnerRequest(
        memberId: Long,
        targetMemberId : Long,
    ) {
        if (memberId == targetMemberId) {
            throw GlobalException(WorkoutPartnerErrorCode.SELF_PARTNER_REQUEST_NOT_ALLOWED)
        }
    }

    @Transactional
    fun updateWorkoutPartnerRequest(memberId: Long, workoutPartnerRequestId : Long, request: WorkoutPartnerUpdateServiceRequest) : WorkoutPartnerRequest {
        val findWorkoutPartnerRequest = workoutPartnerRequestRepository.findByIdAndToMemberId(workoutPartnerRequestId, memberId)
                ?: throw GlobalException(GlobalErrorCode.ENTITY_NOT_FOUND)

        validateDeletedOtherMember(findWorkoutPartnerRequest, memberId)

        validateUpdateWorkoutPartnerRequest(findWorkoutPartnerRequest)

        validateAlreadyWorkoutPartner(findWorkoutPartnerRequest.getFromMemberId(), findWorkoutPartnerRequest.getToMemberId())

        findWorkoutPartnerRequest.updateRespondedAt(time.nowLocalDateTime)

        if(isWorkoutPartnerAccept(request)){

            findWorkoutPartnerRequest.accept()

            val savedWorkoutPartner = addWorkoutPartner(findWorkoutPartnerRequest)

            sendStompAcceptWorkoutPartnerRequest(findWorkoutPartnerRequest)

            sendFcmNotificationAcceptWorkoutPartnerRequest(findWorkoutPartnerRequest.getFromMemberId(), findWorkoutPartnerRequest.getToMemberId())

            sendNotificationAcceptWorkoutPartnerRequest(findWorkoutPartnerRequest, savedWorkoutPartner.id!!)

            return findWorkoutPartnerRequest
        }

        findWorkoutPartnerRequest.reject()

        sendNotificationRejectWorkoutPartnerRequest(findWorkoutPartnerRequest)

        return findWorkoutPartnerRequest
    }

    private fun validateDeletedOtherMember(
        workoutPartnerRequest: WorkoutPartnerRequest,
        memberId: Long
    ) {
        val otherMemberId = if (workoutPartnerRequest.getFromMemberId() == memberId) {
            workoutPartnerRequest.getToMemberId()
        } else {
            workoutPartnerRequest.getFromMemberId()
        }

        memberQueryService.findMemberFromId(otherMemberId)
            ?: throw GlobalException(WorkoutPartnerErrorCode.PARTNER_WITHDRAWN)
    }

    @Transactional
    fun addWorkoutPartner(workoutPartnerRequest: WorkoutPartnerRequest) : WorkoutPartner{
        val workoutPartner =
            WorkoutPartner.of(workoutPartnerRequest.fromMember!!, workoutPartnerRequest.toMember!!)

        return workoutPartnerRepository.save(workoutPartner)
    }


    @Transactional
    fun modifyAllWorkoutPartnerRequestExpire() {
        val response = workoutPartnerRequestRepository.findAllPendingWorkoutPartnerRequestAlreadyExpire()

        val workoutRequestIds = response.map { it.workoutPartnerRequestId }

        workoutPartnerRequestRepository.updateExpireByIdIn(workoutRequestIds)
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

    private fun sendFcmNotificationAcceptWorkoutPartnerRequest(
        memberOneId : Long,
        memberTwoId : Long
    )
    {
        val toFcmEvent = EventFcmWorkoutPartnerAccept(
            toMemberId = memberOneId,
            fromMemberId = memberTwoId
        )
        val fromFcmEvent = EventFcmWorkoutPartnerAccept(
            toMemberId = memberTwoId,
            fromMemberId = memberOneId
        )
        publisher.publishEvent(toFcmEvent)
        publisher.publishEvent(fromFcmEvent)
    }

    private fun sendNotificationAcceptWorkoutPartnerRequest(
        workoutPartnerRequest: WorkoutPartnerRequest,
        workoutPartnerId : Long
    ) {
        val findMemberProfiles = memberQueryService
            .findMemberProfileFromMemberId(
                workoutPartnerRequest.getFromMemberId(),
                workoutPartnerRequest.getToMemberId()
            )
            ?: throw GlobalException(GlobalErrorCode.ENTITY_NOT_FOUND)

        val fromMemberProfile = findMemberProfiles.memberOne
            ?: throw GlobalException(GlobalErrorCode.ENTITY_NOT_FOUND)

        val toMemberProfile = findMemberProfiles.memberTwo
            ?: throw GlobalException(GlobalErrorCode.ENTITY_NOT_FOUND)

        val fromNotification =
            createEventWorkoutPartnerAcceptNotification(fromMemberProfile, toMemberProfile, workoutPartnerRequest, workoutPartnerId)

        val toNotification =
            createEventWorkoutPartnerAcceptNotification(toMemberProfile, fromMemberProfile, workoutPartnerRequest, workoutPartnerId)

        publisher.publishEvent(fromNotification)
        publisher.publishEvent(toNotification)
    }

    private fun createEventWorkoutPartnerAcceptNotification(
        memberOneProfile: MemberProfile,
        memberTwoProfile: MemberProfile,
        workoutPartnerRequest: WorkoutPartnerRequest,
        workoutPartnerId: Long
    ) = EventWorkoutPartnerAccept(
        memberId = memberOneProfile.memberId,
        sender = EventSender(
            memberId = memberTwoProfile.memberId,
        ),
        payload = EventWorkoutPartnerAcceptPayload(
            workoutPartnerRequestId = workoutPartnerRequest.id!!,
            workoutPartnerId = workoutPartnerId,
            memberId = memberTwoProfile.memberId
        )
    )

    private fun sendNotificationWorkoutPartnerRequest(
        fromMemberId: Long,
        toMemberId : Long,
        workoutPartnerRequestId : Long
    ) {
        val findMemberProfile = memberQueryService.findMemberProfileFromMemberId(fromMemberId)
            ?: throw GlobalException(GlobalErrorCode.ENTITY_NOT_FOUND)

        val event = EventWorkoutPartnerRequest(
            memberId = toMemberId,
            sender = EventSender(
                memberId = findMemberProfile.memberId,
            ),
            payload = EventWorkoutPartnerRequestPayload(
                memberId = findMemberProfile.memberId,
                workoutPartnerRequestId = workoutPartnerRequestId
            )
        )

        publisher.publishEvent(event)
    }

    private fun sendFcmWorkoutPartnerRequest(
        toMember: Member,
        fromMember: Member
    ) {
        val fcmEvent = EventFcmWorkoutPartnerRequest(
            toMemberId = toMember.id!!,
            fromNickname = fromMember.nickname!!,
            fromMemberId = fromMember.id!!
        )
        publisher.publishEvent(fcmEvent)
    }

    private fun sendNotificationRejectWorkoutPartnerRequest(workoutPartnerRequest: WorkoutPartnerRequest) {
        val findToMemberProfile =
            memberQueryService.findMemberProfileFromMemberId(workoutPartnerRequest.getToMemberId())
                ?: throw GlobalException(GlobalErrorCode.ENTITY_NOT_FOUND)

        val notificationEvent = EventWorkoutPartnerReject(
            memberId = workoutPartnerRequest.getFromMemberId(),
            sender = EventSender(
                memberId = findToMemberProfile.memberId,
            ),
            payload = EventWorkoutPartnerRejectPayload(
                workoutPartnerRequestId = workoutPartnerRequest.id!!,
                memberId = findToMemberProfile.memberId
            )
        )

        publisher.publishEvent(notificationEvent)
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