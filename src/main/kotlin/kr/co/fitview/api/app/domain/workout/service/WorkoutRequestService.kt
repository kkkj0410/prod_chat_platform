package kr.co.fitview.api.app.domain.workout.service

import kr.co.fitview.api.app.domain.chat.dto.request.ChatWorkoutRequestMessageServiceRequest
import kr.co.fitview.api.app.domain.chat.dto.request.WorkoutRequestUpdateRequest
import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatNoticeMessageType
import kr.co.fitview.api.app.domain.chat.service.ChatMessageService
import kr.co.fitview.api.app.domain.chat.service.ChatNoticeMessageService
import kr.co.fitview.api.app.domain.chat.service.MessageReadStatusService
import kr.co.fitview.api.app.domain.member.dto.response.ChatRoomMemberProfile
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.domain.notification.dto.response.StompEventWorkoutRequestMessageDepth1
import kr.co.fitview.api.app.domain.notification.dto.response.StompEventWorkoutRequestMessageDepth2
import kr.co.fitview.api.app.domain.notification.dto.response.StompEventWorkoutRequestMessageDepth3
import kr.co.fitview.api.app.domain.workout.dto.response.WorkoutRequestUpdateResponse
import kr.co.fitview.api.app.domain.workout.dto.response.enums.WorkoutRequestStatusForRequest
import kr.co.fitview.api.app.domain.workout_history.entity.WorkoutHistory
import kr.co.fitview.api.app.domain.workout.repository.WorkoutRequestRepository
import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequest
import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import kr.co.fitview.api.app.domain.workout_history.service.WorkoutHistoryQueryService
import kr.co.fitview.api.app.domain.workout_history.service.WorkoutHistoryService
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.chat.ChatErrorCode
import kr.co.fitview.api.app.global.exception.error.global.GlobalErrorCode
import kr.co.fitview.api.app.global.exception.error.workout_request.WorkoutRequestErrorCode
import kr.co.fitview.api.app.global.time.Time
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class WorkoutRequestService(
    private val workoutRequestRepository: WorkoutRequestRepository,
    private val workoutHistoryQueryService : WorkoutHistoryQueryService,
    private val workoutHistoryService : WorkoutHistoryService,
    private val chatNoticeMessageService : ChatNoticeMessageService,
    private val chatMessageService : ChatMessageService,
    private val memberQueryService : MemberQueryService,
    private val messageReadStatusService : MessageReadStatusService,
    private val workoutRequestQueryService: WorkoutRequestQueryService,
    private val publisher: ApplicationEventPublisher,
    private val time: Time
) {

    @Transactional
    fun saveChatWorkoutRequestMessage(fromMember: Member, chatRoom: ChatRoom, message: ChatWorkoutRequestMessageServiceRequest) : ChatMessage {
        val now = time.nowLocalDateTime

        validateAddWorkoutRequest(chatRoom, message, now)

        chatRoom.updateLastMessageAt(now)

        val savedChatMessage = chatMessageService.addWorkoutRequestMessage(fromMember, chatRoom, now)

        val toMember = memberQueryService.findOtherMemberFrom(fromMember.id!!, chatRoom.id!!)
            ?: throw GlobalException(GlobalErrorCode.ENTITY_NOT_FOUND)

        val workoutRequest = WorkoutRequest.of(
            chatMessage = savedChatMessage,
            fromMember = fromMember,
            toMember = toMember,
            location = message.location,
            scheduledAt = message.scheduledAt,
            requestedAt = now
        )
        workoutRequestRepository.save(workoutRequest)

        messageReadStatusService.saveMessageReadStatus(
            member = fromMember,
            chatMessage = savedChatMessage,
            chatRoom = chatRoom
        )

        sendStompWorkoutRequestMessage(fromMember, toMember, chatRoom, savedChatMessage, workoutRequest)

        return savedChatMessage
    }


//    @Transactional
//    fun addWorkoutRequest(
//        chatRoomId: Long,
//        chatMessage: ChatMessage,
//        fromMember: Member,
//        toMember: Member,
//        message: ChatWorkoutRequestMessageServiceRequest
//    ): WorkoutRequest {
//        // FE 테스트를 위해 validate 꺼둠. FE 검토 끝나면 다시 켜기(25.12.1)
////        val findWorkoutRequest = findRecentWorkoutRequestFrom(chatRoomId)
////
////        validateExistsWorkoutRequest(findWorkoutRequest)
//
//        val workoutRequest = WorkoutRequest.of(
//            chatMessage = chatMessage,
//            fromMember = fromMember,
//            toMember = toMember,
//            location = message.location,
//            scheduledAt = message.scheduledAt,
//            requestedAt = time.nowLocalDateTime
//        )
//
//        val savedWorkoutRequest = workoutRequestRepository.save(workoutRequest)
//
//        return savedWorkoutRequest
//    }

    @Transactional
    fun modifyAllWorkoutRequestExpire(): List<WorkoutRequestUpdateResponse> {

        val response = workoutRequestRepository.findAllPendingWorkoutRequestAlreadyExpire()

        val workoutRequestIds = response.map { it.workoutRequestId }

        workoutRequestRepository.updateExpireByIdIn(workoutRequestIds)

        val expireResponse = response.map { it.copy(status = WorkoutRequestStatus.EXPIRE) }

        return expireResponse
    }

    @Transactional
    fun modifyWorkoutRequest(memberId: Long, request: WorkoutRequestUpdateRequest): WorkoutRequestUpdateResponse {

        val findWorkoutRequest = workoutRequestRepository
            .findWorkoutRequestByIdAndDeletedAtIsNullWithChatMessage(request.workoutRequestId)
            ?: throw GlobalException(GlobalErrorCode.ENTITY_NOT_FOUND)

        validateWorkoutRequestUpdate(request, findWorkoutRequest, memberId)

        findWorkoutRequest.updateStatus(request.status.toWorkoutRequestStatus())

        var workoutHistory : WorkoutHistory? = null
        if(isSuccessComplete(findWorkoutRequest)){
            workoutHistory = workoutHistoryService.addWorkoutHistory(findWorkoutRequest.getChatRoom()!!, findWorkoutRequest.fromMember!!, findWorkoutRequest.toMember!!)
        }

        addChatNoticeMessage(memberId, request, workoutHistory)

        val response = WorkoutRequestUpdateResponse(
            chatRoomId = findWorkoutRequest.getChatRoomId()!!,
            workoutRequestId = findWorkoutRequest.id!!,
            status = findWorkoutRequest.status!!,
            fromMemberId = findWorkoutRequest.getFromMemberId(),
            toMemberId = findWorkoutRequest.getToMemberId()
        )

        return response
    }

    private fun sendStompWorkoutRequestMessage(
        fromMember: Member,
        toMember: Member,
        chatRoom: ChatRoom,
        chatMessage: ChatMessage,
        workoutRequest: WorkoutRequest,
    ) {
        val isCompleteWorkout = workoutHistoryQueryService.existsWorkoutHistoryFrom(chatRoom.id!!)
        val meProfile = memberQueryService.findMemberProfileFrom(fromMember.id!!, chatRoom.id!!)
            ?: throw GlobalException(GlobalErrorCode.ENTITY_NOT_FOUND)

        val meStompMessage = createStompWorkoutRequestMessage(
            fromMember,
            chatRoom,
            isCompleteWorkout,
            meProfile,
            chatMessage,
            workoutRequest,
            true
        )
        val otherStompMessage = createStompWorkoutRequestMessage(
            toMember,
            chatRoom,
            isCompleteWorkout,
            meProfile,
            chatMessage,
            workoutRequest,
            false
        )
        publisher.publishEvent(meStompMessage)
        publisher.publishEvent(otherStompMessage)
    }

    private fun createStompWorkoutRequestMessage(
        member: Member,
        chatRoom: ChatRoom,
        isCompleteWorkout: Boolean,
        profile: ChatRoomMemberProfile,
        savedChatMessage: ChatMessage,
        workoutRequest: WorkoutRequest,
        isMe : Boolean
    ) : StompEventWorkoutRequestMessageDepth1{
        return StompEventWorkoutRequestMessageDepth1(
            memberId = member.id!!,
            message = StompEventWorkoutRequestMessageDepth2(
                chatRoomId = chatRoom.id!!,
                isCompleteWorkout = isCompleteWorkout,
                profileImageUrl = profile.profileImageUrl,
                nickname = profile.nickname,
                chatMessage = StompEventWorkoutRequestMessageDepth3(
                    chatMessageId = savedChatMessage.id!!,
                    workoutRequestId = workoutRequest.id!!,
                    status = workoutRequest.status!!,
                    scheduledAt = workoutRequest.scheduledAt!!,
                    location = workoutRequest.location!!,
                    sentAt = workoutRequest.requestedAt!!,
                    isMe = isMe
                )
            )
        )
    }

    private fun validateAddWorkoutRequest(
        chatRoom: ChatRoom,
        message: ChatWorkoutRequestMessageServiceRequest,
        now: LocalDateTime
    ) {
        // 12.1 - FE 테스트를 위해 validate 주석처리. 나중에 다시 활성화 필요
//        validateExistsWorkoutRequest(chatRoom.id!!)
        validateScheduledAtNotPast(message.scheduledAt, now)
    }

    private fun validateExistsWorkoutRequest(chatRoomId : Long) {
        val findWorkoutRequest = workoutRequestQueryService.findRecentWorkoutRequestFrom(chatRoomId)

        if (isNotNull(findWorkoutRequest) && isNotFinishStatus(findWorkoutRequest!!)) {
            throw GlobalException(ChatErrorCode.EXISTING_WORKOUT_REQUEST)
        }
    }

    private fun validateScheduledAtNotPast(
        scheduledAt: LocalDateTime,
        now: LocalDateTime
    ) {
        if (scheduledAt.isBefore(now)) {
            throw GlobalException(ChatErrorCode.WORKOUT_REQUEST_TIME_PAST)
        }
    }

    private fun validateWorkoutRequestUpdate(
        request: WorkoutRequestUpdateRequest,
        findWorkoutRequest: WorkoutRequest,
        memberId: Long
    ) {
        validateSameStatus(request, findWorkoutRequest)

        validateNotUpdateStatus(findWorkoutRequest)

        validateNotRejectOrNotCancel(findWorkoutRequest, memberId, request)

        validateNotAcceptFromMember(request, findWorkoutRequest, memberId)

        validateUpdateComplete(request, findWorkoutRequest)
    }

    private fun validateSameStatus(
        request: WorkoutRequestUpdateRequest,
        findWorkoutRequest: WorkoutRequest
    ) {
        if (request.status.toWorkoutRequestStatus() == findWorkoutRequest.status) {
            throw GlobalException(WorkoutRequestErrorCode.ALREADY_SAME_STATUS)
        }
    }

    private fun validateUpdateComplete(
        request: WorkoutRequestUpdateRequest,
        findWorkoutRequest: WorkoutRequest
    ) {
        if (isRequestComplete(request.status) && isNotAccept(findWorkoutRequest.status!!)) {
            throw GlobalException(WorkoutRequestErrorCode.CANNOT_COMPLETE_UNLESS_ACCEPTED)
        }
    }


    private fun isNotFinishStatus(workoutRequest: WorkoutRequest): Boolean =
        workoutRequest.status !in finishStatus

    private val finishStatus = arrayOf(
        WorkoutRequestStatus.REJECT,
        WorkoutRequestStatus.CANCEL,
        WorkoutRequestStatus.COMPLETE,
        WorkoutRequestStatus.EXPIRE
    )

    private fun validateNotUpdateStatus(findWorkoutRequest: WorkoutRequest) {
        val notUpdateStatus = getNotUpdateStatus()

        if (notUpdateStatus.contains(findWorkoutRequest.status)) {
            throw GlobalException(WorkoutRequestErrorCode.TERMINATED_WORKOUT_REQUEST_STATUS_CHANGE)
        }
    }

    private fun validateNotAcceptFromMember(request: WorkoutRequestUpdateRequest, workoutRequest: WorkoutRequest, memberId : Long) {
        if(isFromMember(memberId, workoutRequest) && isAccept(request)){
            throw GlobalException(WorkoutRequestErrorCode.FROM_MEMBER_CANNOT_ACCEPT)
        }
    }


    private fun isFromMember(
        memberId: Long,
        findWorkoutRequest: WorkoutRequest
    ) = memberId == findWorkoutRequest.getFromMemberId()

    private fun addChatNoticeMessage(memberId : Long, request: WorkoutRequestUpdateRequest, workoutHistory : WorkoutHistory?) {
        if (isAccept(request)) {
            chatNoticeMessageService.addChatNoticeFrom(
                memberId,
                request.workoutRequestId,
                ChatNoticeMessageType.WORKOUT_REQUEST_ACCEPT
            )
        }

        if (isComplete(request)) {
            chatNoticeMessageService.addChatNoticeFrom(
                memberId,
                request.workoutRequestId,
                ChatNoticeMessageType.WORKOUT_REQUEST_COMPLETE,
                workoutHistory!!
            )
        }

        if (isReject(request)) {
            chatNoticeMessageService.addChatNoticeFrom(
                memberId,
                request.workoutRequestId,
                ChatNoticeMessageType.WORKOUT_REQUEST_REJECT
            )
        }

        if (isCancel(request)) {
            chatNoticeMessageService.addChatNoticeFrom(
                memberId,
                request.workoutRequestId,
                ChatNoticeMessageType.WORKOUT_REQUEST_CANCEL
            )
        }
    }

    private fun isAccept(request: WorkoutRequestUpdateRequest) =
        request.status == WorkoutRequestStatusForRequest.ACCEPT

    private fun isComplete(request: WorkoutRequestUpdateRequest) =
        request.status == WorkoutRequestStatusForRequest.COMPLETE

    private fun isReject(request: WorkoutRequestUpdateRequest) =
        request.status == WorkoutRequestStatusForRequest.REJECT

    private fun isCancel(request: WorkoutRequestUpdateRequest) =
        request.status == WorkoutRequestStatusForRequest.CANCEL

    private fun getNotUpdateStatus(): List<WorkoutRequestStatus> {
        val notUpdateStatus = enumValues<WorkoutRequestStatus>()
            .filter { it != WorkoutRequestStatus.PENDING && it != WorkoutRequestStatus.ACCEPT }

        return notUpdateStatus
    }

    private fun validateNotRejectOrNotCancel(
        findWorkoutRequest: WorkoutRequest,
        memberId: Long,
        request: WorkoutRequestUpdateRequest
    ) {
        if (isNotReject(findWorkoutRequest, memberId, request)) {
            throw GlobalException(WorkoutRequestErrorCode.FROM_MEMBER_CANNOT_REJECT)
        }

        if (isNotCancel(findWorkoutRequest, memberId, request)) {
            throw GlobalException(WorkoutRequestErrorCode.TO_MEMBER_CANNOT_CANCEL)
        }
    }

    private fun isNotCancel(
        findWorkoutRequest: WorkoutRequest,
        memberId: Long,
        request: WorkoutRequestUpdateRequest
    ) =
        findWorkoutRequest.getToMemberId() == memberId && request.status.toWorkoutRequestStatus() == WorkoutRequestStatus.CANCEL

    private fun isNotReject(
        findWorkoutRequest: WorkoutRequest,
        memberId: Long,
        request: WorkoutRequestUpdateRequest
    ) =
        findWorkoutRequest.getFromMemberId() == memberId && request.status.toWorkoutRequestStatus() == WorkoutRequestStatus.REJECT


    private fun isSuccessComplete(findWorkoutRequest: WorkoutRequest) =
        findWorkoutRequest.status == WorkoutRequestStatus.COMPLETE

    private fun isNotAccept(status: WorkoutRequestStatus) =
        status != WorkoutRequestStatus.ACCEPT

    private fun isRequestComplete(requestStatus: WorkoutRequestStatusForRequest) =
        requestStatus.toWorkoutRequestStatus() == WorkoutRequestStatus.COMPLETE


    private fun isNotNull(value: Any?) = value != null

}