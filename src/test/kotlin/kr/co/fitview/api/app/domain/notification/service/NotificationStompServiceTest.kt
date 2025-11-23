package kr.co.fitview.api.app.domain.notification.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.notification.constant.StompConstant
import kr.co.fitview.api.app.domain.workout.dto.response.WorkoutRequestUpdateResponse
import kr.co.fitview.api.app.domain.workout.entity.QWorkoutRequest.workoutRequest
import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import kr.co.fitview.api.app.global.dto.WsMessageType
import kr.co.fitview.api.app.global.dto.WsResponse
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.then
import org.springframework.beans.factory.annotation.Autowired

class NotificationStompServiceTest @Autowired constructor(
    val notificationStompService: NotificationStompService
) : IntegrationTestSupport(){


    @DisplayName("운동 요청의 상태 변화를 현재 접속한 회원들에게 알린다.")
    @Test
    fun sendWorkoutRequestUpdate() {
        // given

        val workoutRequests = listOf(
            WorkoutRequestUpdateResponse(
                chatRoomId = 1L,
                workoutRequestId = 101L,
                status = WorkoutRequestStatus.EXPIRE,
                fromMemberId = 10L,
                toMemberId = 20L
            ),
            WorkoutRequestUpdateResponse(
                chatRoomId = 2L,
                workoutRequestId = 102L,
                status = WorkoutRequestStatus.EXPIRE,
                fromMemberId = 11L,
                toMemberId = 21L
            )
        )

        given(stompPublisher.sendToUser(any(), any(), any())).willAnswer {}

        // when
        notificationStompService.sendWorkoutRequestUpdate(workoutRequests)

        // then
        workoutRequests.forEach { request ->
            then(stompPublisher).should().sendToUser(
                request.fromMemberId,
                StompConstant.SUB_WORKOUT_REQUEST,
                WsResponse(
                    type = WsMessageType.WORKOUT_REQUEST_UPDATE,
                    payload = request
                )
            )
            then(stompPublisher).should().sendToUser(
                request.toMemberId,
                StompConstant.SUB_WORKOUT_REQUEST,
                WsResponse(
                    type = WsMessageType.WORKOUT_REQUEST_UPDATE,
                    payload = request
                )
            )
        }

    }
}