package kr.co.fitview.api.app.domain.notification.service

import kr.co.fitview.api.app.domain.chat.dto.response.withIsMe
import kr.co.fitview.api.app.domain.notification.constant.StompConstant
import kr.co.fitview.api.app.domain.workout.dto.response.WorkoutRequestUpdateResponse
import kr.co.fitview.api.app.domain.workout.entity.QWorkoutRequest.workoutRequest
import kr.co.fitview.api.app.global.dto.WsMessageType
import kr.co.fitview.api.app.global.dto.WsResponse
import kr.co.fitview.api.app.global.exception.error.ErrorCode
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.stereotype.Service

@Service
class NotificationStompService(
    private val stompPublisher : StompPublisher
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

    fun sendError(memberId : Long, errorCode: ErrorCode) {

        stompPublisher.sendToUser(
            memberId = memberId,
            destination = StompConstant.SUB_ERROR,
            payload = WsResponse(
                type = errorCode.code,
                payload = errorCode.message
            )
        )
    }

    fun sendOtherError(memberId: Long, root: Throwable) {

        stompPublisher.sendToUser(
            memberId = memberId,
            destination = StompConstant.SUB_ERROR,
            payload = WsResponse(
                type = root.javaClass.simpleName,
                payload = (root.message ?: "Unknown error")
            )
        )

    }


}