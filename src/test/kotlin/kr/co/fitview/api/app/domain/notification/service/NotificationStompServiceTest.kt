package kr.co.fitview.api.app.domain.notification.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.dto.response.*
import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatNoticeMessageType
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.chat.repository.*
import kr.co.fitview.api.app.domain.chat.service.ChatNoticeMessageService
import kr.co.fitview.api.app.domain.member.dto.response.MemberChatProfileResponse
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.notification.constant.StompConstant
import kr.co.fitview.api.app.domain.notification.dto.StompSendEvent
import kr.co.fitview.api.app.domain.notification.dto.response.*
import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.domain.workout.dto.response.WorkoutRequestUpdateResponse
import kr.co.fitview.api.app.domain.workout.dto.response.enums.WorkoutRequestStatusForResponse
import kr.co.fitview.api.app.domain.workout.entity.WorkoutHistory
import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequest
import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import kr.co.fitview.api.app.domain.workout.repository.WorkoutHistoryRepository
import kr.co.fitview.api.app.domain.workout.repository.WorkoutRequestRepository
import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartnerRequest
import kr.co.fitview.api.app.domain.workout_partner.entity.enums.WorkoutPartnerRequestContent
import kr.co.fitview.api.app.domain.workout_partner.repository.WorkoutPartnerRequestRepository
import kr.co.fitview.api.app.global.dto.WsMessageType
import kr.co.fitview.api.app.global.dto.WsResponse
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.exception.error.chat.ChatErrorCode
import kr.co.fitview.api.app.global.time.Time
import kr.co.fitview.api.app.global.util.TestDataFactory
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.then
import org.springframework.beans.factory.annotation.Autowired

class NotificationStompServiceTest @Autowired constructor(
    val notificationStompService: NotificationStompService,
    val memberRepository: MemberRepository,
    val workoutPartnerRequestRepository : WorkoutPartnerRequestRepository,
    val oAuth2Service: OAuth2Service,
    val time: Time,
    private val chatRoomRepository : ChatRoomRepository,
    private val chatParticipantRepository : ChatParticipantRepository,
    private val chatMessageRepository : ChatMessageRepository,
    private val workoutRequestRepository : WorkoutRequestRepository,
    private val workoutHistoryRepository: WorkoutHistoryRepository
) : IntegrationTestSupport() {

    @DisplayName("채팅 메시지를 송신자, 수신자에게 보낸다.")
    @Test
    fun sendChatTextMessage() {
        // given
        val memberId = 123L
        val otherMemberId = 234L

        val chatMessage = StompChatTextMessage(
            chatMessageId = 1L,
            sentAt = time.nowLocalDateTime,
            content = "하드코딩 메시지 예시입니다."
        )

        val chatProfile = MemberChatProfileResponse(
            profileImageUrl = "https://example.com/profile.jpg",
            nickname = "홍길동"
        )

        val response = ChatMessageDetailResponse.of(
            chatRoomId = 123L,
            isCompleteWorkout = false,
            chatProfile = chatProfile,
            chatMessage = chatMessage,
            otherMemberId = otherMemberId
        )

        given(stompPublisher.sendToUser(any())).willAnswer {}


        // when
        notificationStompService.sendChatNoticeMessage(memberId, response)

        // then
        then(stompPublisher).should().sendToUser(
            StompSendEvent(
                memberId = memberId,
                destination = StompConstant.SUB_CHAT_MESSAGE,
                payload = WsResponse(
                    type = WsMessageType.TEXT.code,
                    payload = response.withIsMe(true)
                )
            )
        )
        then(stompPublisher).should().sendToUser(
            StompSendEvent(
                memberId = otherMemberId,
                destination = StompConstant.SUB_CHAT_MESSAGE,
                payload = WsResponse(
                    type = WsMessageType.TEXT.code,
                    payload = response.withIsMe(false)
                )
            )
        )
    }

    @DisplayName("채팅 운동 요청 메시지를 송신자, 수신자에게 보낸다.")
    @Test
    fun sendChatWorkoutRequestMessage() {
        // given
        val memberId = 123L
        val otherMemberId = 234L

        val workoutMessage = StompChatWorkoutRequestMessage(
            chatMessageId = 2L,
            sentAt = time.nowLocalDateTime,
            workoutRequestId = 987L,
            status = WorkoutRequestStatusForResponse.PENDING,
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            location = "location",
        )

        val chatProfile = MemberChatProfileResponse(
            profileImageUrl = "https://example.com/profile.jpg",
            nickname = "홍길동"
        )

        val response = ChatMessageDetailResponse.of(
            chatRoomId = 123L,
            isCompleteWorkout = false,
            chatProfile = chatProfile,
            chatMessage = workoutMessage,
            otherMemberId = otherMemberId
        )

        given(stompPublisher.sendToUser(any())).willAnswer {}


        // when
        notificationStompService.sendChatNoticeMessage(memberId, response)

        // then
        then(stompPublisher).should().sendToUser(
            StompSendEvent(
                memberId = memberId,
                destination = StompConstant.SUB_CHAT_MESSAGE,
                payload = WsResponse(
                    type = WsMessageType.WORKOUT_REQUEST.code,
                    payload = response.withIsMe(true)
                )
            )
        )
        then(stompPublisher).should().sendToUser(
            StompSendEvent(
                memberId = otherMemberId,
                destination = StompConstant.SUB_CHAT_MESSAGE,
                payload = WsResponse(
                    type = WsMessageType.WORKOUT_REQUEST.code,
                    payload = response.withIsMe(false)
                )
            )
        )
    }

    @DisplayName("채팅 안내 문구 메시지를 송신자, 수신자에게 보낸다.")
    @Test
    fun sendChatNoticeMessage() {
        // given
        val memberId = 123L
        val otherMemberId = 234L

        val chatMessage = StompChatNoticeMessage(
            chatMessageId = 1L,
            sentAt = time.nowLocalDateTime,
            content = ChatNoticeMessageType.WORKOUT_REQUEST_COMPLETE,
            workoutHistoryId = 123
        )

        val chatProfile = MemberChatProfileResponse(
            profileImageUrl = "https://example.com/profile.jpg",
            nickname = "홍길동"
        )

        val response = ChatMessageDetailResponse.of(
            chatRoomId = 123L,
            isCompleteWorkout = false,
            chatProfile = chatProfile,
            chatMessage = chatMessage,
            otherMemberId = otherMemberId
        )

        given(stompPublisher.sendToUser(any())).willAnswer {}


        // when
        notificationStompService.sendChatNoticeMessage(memberId, response)

        // then
        then(stompPublisher).should().sendToUser(
            StompSendEvent(
                memberId = memberId,
                destination = StompConstant.SUB_CHAT_MESSAGE,
                payload = WsResponse(
                    type = WsMessageType.NOTICE.code,
                    payload = response
                )
            )
        )
        then(stompPublisher).should().sendToUser(
            StompSendEvent(
                memberId = otherMemberId,
                destination = StompConstant.SUB_CHAT_MESSAGE,
                payload = WsResponse(
                    type = WsMessageType.NOTICE.code,
                    payload = response
                )
            )
        )
    }


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

        given(stompPublisher.sendToUser(any())).willAnswer {}

        // when
        notificationStompService.sendWorkoutRequestUpdate(workoutRequests)

        // then
        workoutRequests.forEach { request ->
            then(stompPublisher).should().sendToUser(
                StompSendEvent(
                    memberId = request.fromMemberId,
                    destination = StompConstant.SUB_WORKOUT_REQUEST,
                    payload = WsResponse(
                        type = WsMessageType.WORKOUT_REQUEST_UPDATE.code,
                        payload = request
                    )
                )
            )
            then(stompPublisher).should().sendToUser(
                StompSendEvent(
                    memberId = request.toMemberId,
                    destination = StompConstant.SUB_WORKOUT_REQUEST,
                    payload = WsResponse(
                        type = WsMessageType.WORKOUT_REQUEST_UPDATE.code,
                        payload = request
                    )
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

        given(stompPublisher.sendToUser(any())).willAnswer {}

        val workoutPartnerRequest = WorkoutPartnerRequest.of(
            fromMember = fromMember,
            toMember = toMember,
            now = time.nowLocalDateTime,
            content = WorkoutPartnerRequestContent.BURN
        )
        workoutPartnerRequestRepository.save(workoutPartnerRequest)

        val response = MemberWorkoutPartnerRequestProfileResponse(
            memberId = fromMember.id!!,
            profileImageUrl = signupRequest.profileImageUrl,
            nickname = signupRequest.nickname,
            workoutPartnerRequestId = workoutPartnerRequest.id!!
        )


        // when
        notificationStompService.sendWorkoutPartnerRequest(
            workoutPartnerRequest = workoutPartnerRequest
        )

        // then
        then(stompPublisher).should().sendToUser(
            StompSendEvent(
                memberId = toMember.id!!,
                destination = StompConstant.SUB_WORKOUT_PARTNER,
                payload = WsResponse(
                    type = WsMessageType.WORKOUT_PARTNER_REQUEST.code,
                    payload = response
                )
            )
        )
    }

    @DisplayName("운동 파트너 승인을 처음 요청자에게 전달한다.")
    @Test
    fun sendWorkoutPartnerAccept() {
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
        oAuth2Service.signup(signupRequest, toMember.id!!)

        given(stompPublisher.sendToUser(any())).willAnswer {}

        val workoutPartnerRequest = WorkoutPartnerRequest.of(
            fromMember = fromMember,
            toMember = toMember,
            now = time.nowLocalDateTime,
            content = WorkoutPartnerRequestContent.BURN
        )
        workoutPartnerRequestRepository.save(workoutPartnerRequest)

        val response = MemberWorkoutPartnerRequestAcceptProfileResponse(
            memberId = toMember.id!!,
            profileImageUrl = signupRequest.profileImageUrl,
            nickname = signupRequest.nickname,
            workoutPartnerRequestContentIndex = workoutPartnerRequest.content!!.index
        )

        // when
        notificationStompService.sendWorkoutPartnerAccept(
            workoutPartnerRequest = workoutPartnerRequest
        )

        // then
        then(stompPublisher).should().sendToUser(
            StompSendEvent(
                memberId = fromMember.id!!,
                destination = StompConstant.SUB_WORKOUT_PARTNER,
                payload = WsResponse(
                    type = WsMessageType.WORKOUT_PARTNER_ACCEPT.code,
                    payload = response
                )
            )
        )
    }


    @DisplayName("사용자 정의 예외를 실시간으로 회원에게 전달한다.")
    @Test
    fun sendGlobalError() {
        // given
        val memberId = 123L
        val errorCode = ChatErrorCode.NOT_PARTNER

        given(stompPublisher.sendToUser(any())).willAnswer {}

        // when
        notificationStompService.sendGlobalError(
            memberId = memberId,
            errorCode = errorCode
        )

        // then
        then(stompPublisher).should().sendToUser(
            StompSendEvent(
                memberId = memberId,
                destination = StompConstant.SUB_ERROR,
                payload = WsResponse(
                    type = errorCode.code,
                    payload = errorCode.message
                )
            )
        )
    }

    @DisplayName("기본 예외를 실시간으로 회원에게 전달한다.")
    @Test
    fun sendOtherError() {
        // given
        val memberId = 123L
        val ex = RuntimeException("exception")

        given(stompPublisher.sendToUser(any())).willAnswer {}

        // when
        notificationStompService.sendOtherError(
            memberId = memberId,
            ex = ex
        )

        // then
        then(stompPublisher).should().sendToUser(
            StompSendEvent(
                memberId = memberId,
                destination = StompConstant.SUB_ERROR,
                payload = WsResponse(
                    type = ex.javaClass.simpleName,
                    payload = ex.message ?: "Unknown error"
                )
            )
        )
    }

    @DisplayName("안내 문구 메시지를 실시간으로 보낸다.")
    @Test
    fun sendChatNoticeMessageByEvent() {
        // given
        val me = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        val other = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(me)
        memberRepository.save(other)

        val signupRequest = TestDataFactory.oAuth2SignupRequest()
        oAuth2Service.signup(signupRequest, me.id!!)

        val signupRequest2 = TestDataFactory.oAuth2SignupRequest(
            profileImageUrl = "updateImage1",
            nickname = "update1"
        )
        oAuth2Service.signup(signupRequest2, other.id!!)

        val chatRoom = chatRoomRepository.save(ChatRoom(ChatRoomType.PRIVATE))

        val chatParticipant1 = ChatParticipant(
            chatRoom,
            me
        )
        val chatParticipant2 = ChatParticipant(
            chatRoom,
            other
        )
        chatParticipantRepository.save(chatParticipant1)
        chatParticipantRepository.save(chatParticipant2)


        val chatMessage1 = ChatMessage.ofWorkoutRequest(
            member = me,
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage1)

        val workoutRequest = WorkoutRequest.of(
            chatMessage = chatMessage1,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        workoutRequestRepository.save(workoutRequest)


        val stomp1 = StompChatNoticeMessageResponse(
            memberId = me.id!!,
            message = StompEventChatNoticeMessage(
                chatRoomId = chatRoom.id!!,
                isCompleteWorkout = false,
                profileImageUrl = signupRequest2.profileImageUrl,
                nickname = signupRequest2.nickname,
                chatMessage = StompNoticeMessage(
                    chatMessageId = chatMessage1.id!!,
                    sentAt = chatMessage1.sentAt!!,
                    workoutHistoryId = null,
                    content = ChatNoticeMessageType.WORKOUT_REQUEST_EXPIRE
                )
            )
        )

        val stomp2 = StompChatNoticeMessageResponse(
            memberId = other.id!!,
            message = StompEventChatNoticeMessage(
                chatRoomId = chatRoom.id!!,
                isCompleteWorkout = false,
                profileImageUrl = signupRequest.profileImageUrl,
                nickname = signupRequest.nickname,
                chatMessage = StompNoticeMessage(
                    chatMessageId = chatMessage1.id!!,
                    sentAt = chatMessage1.sentAt!!,
                    workoutHistoryId = null,
                    content = ChatNoticeMessageType.WORKOUT_REQUEST_EXPIRE
                )
            )
        )

        // when
        notificationStompService.sendChatNoticeMessage(stomp1)
        notificationStompService.sendChatNoticeMessage(stomp2)

        // then
        then(stompPublisher).should().sendToUser(
            memberId = me.id!!,
            destination = StompConstant.SUB_CHAT_MESSAGE,
            payload = WsResponse(
                type = WsMessageType.NOTICE.code,
                payload = stomp1.message
            )
        )
        then(stompPublisher).should().sendToUser(
            memberId = other.id!!,
            destination = StompConstant.SUB_CHAT_MESSAGE,
            payload = WsResponse(
                type = WsMessageType.NOTICE.code,
                payload = stomp2.message
            )
        )
    }


}