package kr.co.fitview.api.app.domain.notification.service

import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.domain.notification.constant.StompConstant
import kr.co.fitview.api.app.domain.workout.dto.response.WorkoutRequestUpdateResponse
import kr.co.fitview.api.app.domain.workout.entity.QWorkoutRequest.workoutRequest
import kr.co.fitview.api.app.global.dto.WsMessageType
import kr.co.fitview.api.app.global.dto.WsResponse
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.ErrorCode
import kr.co.fitview.api.app.global.exception.error.global.GlobalErrorCode
import org.springframework.stereotype.Service

@Service
class NotificationStompService(
    private val stompPublisher : StompPublisher,
    private val memberQueryService : MemberQueryService
) {


    fun sendWorkoutRequestUpdate(workoutRequests : List<WorkoutRequestUpdateResponse>) {
        if(workoutRequests.isEmpty()){
            return
        }

        workoutRequests.forEach{workoutRequest->
            stompPublisher.sendToUser(
                memberId = workoutRequest.fromMemberId,
                destination = StompConstant.SUB_WORKOUT_REQUEST,
                payload = WsResponse(
                    type = WsMessageType.WORKOUT_REQUEST_UPDATE.code,
                    payload = workoutRequest
                )
            )

            stompPublisher.sendToUser(
                memberId = workoutRequest.toMemberId,
                destination = StompConstant.SUB_WORKOUT_REQUEST,
                payload = WsResponse(
                    type = WsMessageType.WORKOUT_REQUEST_UPDATE.code,
                    payload = workoutRequest
                )
            )
        }
    }

    fun sendWorkoutPartnerRequest(fromMemberId : Long, toMemberId : Long){
        val findMemberProfile = memberQueryService.findMemberWorkoutRequestProfileFrom(fromMemberId)
            ?: throw GlobalException(GlobalErrorCode.ENTITY_NOT_FOUND)

        stompPublisher.sendToUser(
            memberId = toMemberId,
            destination = StompConstant.SUB_WORKOUT_PARTNER,
            payload = WsResponse(
                type = WsMessageType.WORKOUT_PARTNER_REQUEST.code,
                payload = findMemberProfile
            )
        )

    }

    fun sendGlobalError(memberId : Long, errorCode: ErrorCode) {
        stompPublisher.sendToUser(
            memberId = memberId,
            destination = StompConstant.SUB_ERROR,
            payload = WsResponse(
                type = errorCode.code,
                payload = errorCode.message
            )
        )
    }

    fun sendOtherError(memberId: Long, ex: Throwable) {
        stompPublisher.sendToUser(
            memberId = memberId,
            destination = StompConstant.SUB_ERROR,
            payload = WsResponse(
                type = ex.javaClass.simpleName,
                payload = (ex.message ?: "Unknown error")
            )
        )
    }


}