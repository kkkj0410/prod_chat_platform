package kr.co.fitview.api.app.domain.notification.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.member.dto.response.MemberWorkoutPartnerProfileResponse
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.notification.constant.StompConstant
import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.domain.workout.dto.response.WorkoutRequestUpdateResponse
import kr.co.fitview.api.app.domain.workout.entity.QWorkoutRequest.workoutRequest
import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import kr.co.fitview.api.app.global.dto.WsMessageType
import kr.co.fitview.api.app.global.dto.WsResponse
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.exception.error.chat.ChatErrorCode
import kr.co.fitview.api.app.global.util.TestDataFactory
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.then
import org.springframework.beans.factory.annotation.Autowired

class NotificationStompServiceTest @Autowired constructor(
    val notificationStompService: NotificationStompService,
    val memberRepository: MemberRepository,
    val oAuth2Service: OAuth2Service
) : IntegrationTestSupport() {


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
                    type = WsMessageType.WORKOUT_REQUEST_UPDATE.code,
                    payload = request
                )
            )
            then(stompPublisher).should().sendToUser(
                request.toMemberId,
                StompConstant.SUB_WORKOUT_REQUEST,
                WsResponse(
                    type = WsMessageType.WORKOUT_REQUEST_UPDATE.code,
                    payload = request
                )
            )
        }
    }

    @DisplayName("운동 파트너 요청을 수신자에게 전달한다.")
    @Test
    fun sendWorkoutPartnerRequest() {
        // given
        val fromMember = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )

        val toMember = Member(
            email = "email2",
            password = "password2",
            role = Role.USER,
        )
        memberRepository.save(fromMember)
        memberRepository.save(toMember)

        val signupRequest = TestDataFactory.oAuth2SignupRequest()
        oAuth2Service.signup(signupRequest, fromMember.id!!)

        given(stompPublisher.sendToUser(any(), any(), any())).willAnswer {}

        val response = MemberWorkoutPartnerProfileResponse(
            memberId = fromMember.id!!,
            profileImageUrl = signupRequest.profileImageUrl,
            nickname = signupRequest.nickname
        )

        // when
        notificationStompService.sendWorkoutPartnerRequest(
            fromMemberId = fromMember.id!!,
            toMemberId = toMember.id!!
        )

        // then
        then(stompPublisher).should().sendToUser(
            toMember.id!!,
            StompConstant.SUB_WORKOUT_PARTNER,
            WsResponse(
                type = WsMessageType.WORKOUT_PARTNER_REQUEST.code,
                payload = response
            )
        )
    }


    @DisplayName("사용자 정의 예외를 실시간으로 회원에게 전달한다.")
    @Test
    fun sendGlobalError() {
        // given
        val memberId = 123L
        val errorCode = ChatErrorCode.NOT_PARTNER

        given(stompPublisher.sendToUser(any(), any(), any())).willAnswer {}

        // when
        notificationStompService.sendGlobalError(
            memberId = memberId,
            errorCode = errorCode
        )

        // then
        then(stompPublisher).should().sendToUser(
            memberId,
            StompConstant.SUB_ERROR,
            WsResponse(
                type = errorCode.code,
                payload = errorCode.message
            )
        )
    }

    @DisplayName("기본 예외를 실시간으로 회원에게 전달한다.")
    @Test
    fun sendOtherError() {
        // given
        val memberId = 123L
        val ex = RuntimeException("exception")

        given(stompPublisher.sendToUser(any(), any(), any())).willAnswer {}

        // when
        notificationStompService.sendOtherError(
            memberId = memberId,
            ex = ex
        )

        // then
        then(stompPublisher).should().sendToUser(
            memberId,
            StompConstant.SUB_ERROR,
            WsResponse(
                type = ex.javaClass.simpleName,
                payload = ex.message ?: "Unknown error"
            )
        )
    }


}