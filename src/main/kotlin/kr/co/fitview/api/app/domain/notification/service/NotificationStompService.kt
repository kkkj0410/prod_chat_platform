package kr.co.fitview.api.app.domain.notification.service

import kr.co.fitview.api.app.domain.chat.dto.response.withIsMe
import kr.co.fitview.api.app.domain.notification.constant.StompConstant
import kr.co.fitview.api.app.domain.workout.dto.response.WorkoutRequestUpdateResponse
import kr.co.fitview.api.app.domain.workout.entity.QWorkoutRequest.workoutRequest
import kr.co.fitview.api.app.global.dto.WsMessageType
import kr.co.fitview.api.app.global.dto.WsResponse
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

        WsResponse(
            type = WsMessageType.WORKOUT_REQUEST,
            payload = workoutRequest
        )

        workoutRequests.forEach{workoutRequest->
            stompPublisher.sendToUser(
                memberId = workoutRequest.fromMemberId,
                destination = StompConstant.SUB_WORKOUT_REQUEST,
                payload = WsResponse(
                    type = WsMessageType.WORKOUT_REQUEST_UPDATE,
                    payload = workoutRequest
                )
            )

            stompPublisher.sendToUser(
                memberId = workoutRequest.toMemberId,
                destination = StompConstant.SUB_WORKOUT_REQUEST,
                payload = WsResponse(
                    type = WsMessageType.WORKOUT_REQUEST_UPDATE,
                    payload = workoutRequest
                )
            )
        }
    }
}