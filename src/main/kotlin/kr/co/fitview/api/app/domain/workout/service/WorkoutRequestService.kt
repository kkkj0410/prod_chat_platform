package kr.co.fitview.api.app.domain.workout.service

import kr.co.fitview.api.app.domain.chat.dto.request.ChatWorkoutRequestMessageServiceRequest
import kr.co.fitview.api.app.domain.chat.dto.request.WorkoutRequestUpdateRequest
import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.workout.dto.response.LastWorkoutRequestMessage
import kr.co.fitview.api.app.domain.workout.dto.response.WorkoutRequestUpdateResponse
import kr.co.fitview.api.app.domain.workout.repository.WorkoutRequestRepository
import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequest
import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.chat.ChatErrorCode
import kr.co.fitview.api.app.global.exception.error.global.GlobalErrorCode
import kr.co.fitview.api.app.global.exception.error.workout_request.WorkoutRequestErrorCode
import kr.co.fitview.api.app.global.time.Time
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class WorkoutRequestService(
    private val workoutRequestRepository: WorkoutRequestRepository,
    private val time: Time
) {

    @Transactional
    fun addWorkoutRequest(
        chatRoomId: Long,
        chatMessage: ChatMessage,
        fromMember: Member,
        toMember: Member,
        message: ChatWorkoutRequestMessageServiceRequest
    ): WorkoutRequest {
        val findWorkoutRequest = findRecentWorkoutRequestFrom(chatRoomId)

        validateExistsWorkoutRequest(findWorkoutRequest)

        val workoutRequest = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = fromMember,
            toMember = toMember,
            location = message.location,
            scheduledAt = message.scheduledAt,
            requestedAt = time.nowLocalDateTime
        )

        return workoutRequestRepository.save(workoutRequest)
    }

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

        validateNotUpdateStatus(findWorkoutRequest)

        validateNotRejectOrNotCancel(findWorkoutRequest, memberId, request)

        findWorkoutRequest.updateStatus(request.status.toWorkoutRequestStatus())

        val response = WorkoutRequestUpdateResponse(
            chatRoomId = findWorkoutRequest.getChatRoomId()!!,
            workoutRequestId = findWorkoutRequest.id!!,
            status = findWorkoutRequest.status!!,
            fromMemberId = findWorkoutRequest.getFromMemberId(),
            toMemberId = findWorkoutRequest.getToMemberId()
        )

        return response
    }

    fun findRecentWorkoutRequestFrom(chatRoomIds: List<Long>): List<LastWorkoutRequestMessage> {
        return workoutRequestRepository.findRecentWorkoutRequest(chatRoomIds)
    }

    fun findRecentWorkoutRequestFrom(chatRoomId: Long): WorkoutRequest? {
        return workoutRequestRepository.findRecentWorkoutRequestEntity(chatRoomId)
    }


    private fun validateExistsWorkoutRequest(findWorkoutRequest: WorkoutRequest?) {
        if (isNotNull(findWorkoutRequest) && isNotFinishStatus(findWorkoutRequest!!)) {
            throw GlobalException(ChatErrorCode.EXISTING_WORKOUT_REQUEST)
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

    private fun getNotUpdateStatus(): List<WorkoutRequestStatus> {
        val notUpdateStatus = enumValues<WorkoutRequestStatus>()
            .filter { it != WorkoutRequestStatus.PENDING }

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


    private fun isNotNull(value: Any?) = value != null

}