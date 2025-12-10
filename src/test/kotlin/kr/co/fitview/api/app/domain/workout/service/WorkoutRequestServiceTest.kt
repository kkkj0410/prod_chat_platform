package kr.co.fitview.api.app.domain.workout.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.dto.request.ChatWorkoutRequestMessageServiceRequest
import kr.co.fitview.api.app.domain.chat.dto.request.WorkoutRequestUpdateRequest
import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatNoticeMessageType
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.chat.repository.*
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.notification.dto.request.EventWorkoutPartnerRequest
import kr.co.fitview.api.app.domain.notification.dto.request.EventWorkoutRequest
import kr.co.fitview.api.app.domain.notification.dto.request.EventWorkoutRequestAccept
import kr.co.fitview.api.app.global.stomp.dto.request.StompEventUpdateWorkoutRequestMessageDepth1
import kr.co.fitview.api.app.global.stomp.dto.request.StompEventWorkoutRequestMessageDepth1
import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.domain.workout.dto.response.enums.WorkoutRequestStatusForRequest
import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequest
import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import kr.co.fitview.api.app.domain.workout_history.repository.WorkoutHistoryRepository
import kr.co.fitview.api.app.domain.workout.repository.WorkoutRequestRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.chat.ChatErrorCode
import kr.co.fitview.api.app.global.exception.error.workout_request.WorkoutRequestErrorCode
import kr.co.fitview.api.app.global.time.Time
import kr.co.fitview.api.app.global.util.TestDataFactory
import org.assertj.core.api.Assertions.*
import org.assertj.core.api.ThrowingConsumer
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired

class WorkoutRequestServiceTest @Autowired constructor(
    val workoutRequestRepository : WorkoutRequestRepository,
    val workoutRequestService: WorkoutRequestService,
    val workoutHistoryRepository : WorkoutHistoryRepository,
    val memberRepository : MemberRepository,
    val chatRoomRepository : ChatRoomRepository,
    val chatParticipantRepository : ChatParticipantRepository,
    val chatMessageRepository : ChatMessageRepository,
    val chatNoticeMessageRepository : ChatNoticeMessageRepository,
    val oAuth2Service : OAuth2Service,
    val messageReadStatusRepository: MessageReadStatusRepository,
    val time : Time
) : IntegrationTestSupport() {

    @DisplayName("회원은 WORKOUT_REQUEST 메시지를 보낸다.")
    @Test
    fun saveChatWorkoutRequestMessage() {
        // given
        val me = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        val other = Member(
            email = "email2",
            password = "password2",
            role = Role.USER,
        )
        memberRepository.save(me)
        memberRepository.save(other)

        val signupRequest = TestDataFactory.oAuth2SignupRequest()
        oAuth2Service.signup(signupRequest, me.id!!)

        val savedChatRoom = chatRoomRepository.save(ChatRoom(ChatRoomType.PRIVATE))

        val chatParticipant1 = ChatParticipant(
            savedChatRoom,
            me
        )
        val chatParticipant2 = ChatParticipant(
            savedChatRoom,
            other
        )
        chatParticipantRepository.save(chatParticipant1)
        chatParticipantRepository.save(chatParticipant2)

        val request = ChatWorkoutRequestMessageServiceRequest(
            scheduledAt = time.nowLocalDateTime.plusHours(5),
            location = "location"
        )

        // when
        workoutRequestService.saveChatWorkoutRequestMessage(
            fromMember = me,
            chatRoom = savedChatRoom,
            message = request
        )

        // then
        val findChatMessage = chatMessageRepository.findAll()[0]
        val findWorkoutRequest = workoutRequestRepository.findAll()[0]
        val findMessageReadStatuses = messageReadStatusRepository.findAll()

        assertThat(findChatMessage)
            .extracting("member", "chatRoom", "type", "content", "sentAt")
            .contains(me, savedChatRoom, ChatMessageType.WORKOUT_REQUEST, null, time.nowLocalDateTime)

        assertThat(findWorkoutRequest)
            .extracting("fromMember", "toMember", "status", "location", "scheduledAt", "requestedAt")
            .contains(
                me,
                other,
                WorkoutRequestStatus.PENDING,
                request.location,
                request.scheduledAt,
                time.nowLocalDateTime
            )

        assertThat(savedChatRoom.lastMessageAt).isEqualTo(time.nowLocalDateTime)

        assertThat(findMessageReadStatuses)
            .extracting("chatRoom", "member", "chatMessage", "isRead")
            .containsExactlyInAnyOrder(
                tuple(savedChatRoom, me, findChatMessage, true),
                tuple(savedChatRoom, other, findChatMessage, false),
            )
    }

    @DisplayName("회원은 운동 요청 메시지를 보낼 시, 실시간 알림이 발생한다.")
    @Test
    fun saveChatWorkoutRequestMessageStomp() {
        // given
        val me = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        val other = Member(
            email = "email2",
            password = "password2",
            role = Role.USER,
        )
        memberRepository.save(me)
        memberRepository.save(other)

        val signupRequest = TestDataFactory.oAuth2SignupRequest()
        oAuth2Service.signup(signupRequest, me.id!!)

        val savedChatRoom = chatRoomRepository.save(ChatRoom(ChatRoomType.PRIVATE))

        val chatParticipant1 = ChatParticipant(
            savedChatRoom,
            me
        )
        val chatParticipant2 = ChatParticipant(
            savedChatRoom,
            other
        )
        chatParticipantRepository.save(chatParticipant1)
        chatParticipantRepository.save(chatParticipant2)

        val request = ChatWorkoutRequestMessageServiceRequest(
            scheduledAt = time.nowLocalDateTime.plusHours(5),
            location = "location"
        )

        // when
        workoutRequestService.saveChatWorkoutRequestMessage(
            fromMember = me,
            chatRoom = savedChatRoom,
            message = request
        )

        // then
        val count = events.stream(StompEventWorkoutRequestMessageDepth1::class.java).count()
        assertThat(count).isEqualTo(2)
    }


    @DisplayName("회원은 운동 요청 메시지를 보낼 시, 상대방에게 인앱 알림이 발생한다.")
    @Test
    fun saveChatWorkoutRequestMessageNotification() {
        // given
        val me = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        val other = Member(
            email = "email2",
            password = "password2",
            role = Role.USER,
        )
        memberRepository.save(me)
        memberRepository.save(other)

        val signupRequest = TestDataFactory.oAuth2SignupRequest()
        oAuth2Service.signup(signupRequest, me.id!!)

        val savedChatRoom = chatRoomRepository.save(ChatRoom(ChatRoomType.PRIVATE))

        val chatParticipant1 = ChatParticipant(
            savedChatRoom,
            me
        )
        val chatParticipant2 = ChatParticipant(
            savedChatRoom,
            other
        )
        chatParticipantRepository.save(chatParticipant1)
        chatParticipantRepository.save(chatParticipant2)

        val request = ChatWorkoutRequestMessageServiceRequest(
            scheduledAt = time.nowLocalDateTime.plusHours(5),
            location = "location"
        )

        // when
        workoutRequestService.saveChatWorkoutRequestMessage(
            fromMember = me,
            chatRoom = savedChatRoom,
            message = request
        )

        // then
        val count = events.stream(EventWorkoutRequest::class.java).count()
        assertThat(count).isEqualTo(1)
    }

    @DisplayName("운동 요청이 이미 존재하지만 끝난 상태이면 다시 운동 요청 가능")
    @ParameterizedTest(name = "case {index}: 기존 요청 상태 = {0}")
    @CsvSource("REJECT", "CANCEL", "COMPLETE", "EXPIRE")
    fun saveChatWorkoutRequestMessageWhenExistsEndedWorkoutRequest(status : String) {
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
        workoutRequest.updateStatus(WorkoutRequestStatus.valueOf(status))
        workoutRequestRepository.save(workoutRequest)


        val chatMessage2 = ChatMessage.ofWorkoutRequest(
            member = me,
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage2)

        val request = ChatWorkoutRequestMessageServiceRequest(
            type = ChatMessageType.WORKOUT_REQUEST,
            scheduledAt = time.nowLocalDateTime.plusHours(24),
            location = "location"
        )

        // when
        val savedChatMessage = workoutRequestService.saveChatWorkoutRequestMessage(
            chatRoom = chatRoom,
            fromMember = me,
            message = request
        )

        // then
        assertThat(savedChatMessage.id).isNotNull()
        assertThat(savedChatMessage)
            .extracting("member", "chatRoom", "type", "content", "sentAt")
            .contains(me, chatRoom, ChatMessageType.WORKOUT_REQUEST, null, time.nowLocalDateTime)

    }


    @DisplayName("운동 요청이 이미 존재하면 운동 요청을 보낼 수 없다")
//    @ParameterizedTest(name = "case {index}: 기존 요청 상태 = {0}")
//    @CsvSource("PENDING", "ACCEPT",)
    fun saveChatWorkoutRequestMessageWhenExistsWorkoutRequest(status : String) {
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
        workoutRequest.updateStatus(WorkoutRequestStatus.valueOf(status))
        workoutRequestRepository.save(workoutRequest)

        val chatMessage2 = ChatMessage.ofWorkoutRequest(
            member = me,
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage2)

        val request = ChatWorkoutRequestMessageServiceRequest(
            type = ChatMessageType.WORKOUT_REQUEST,
            scheduledAt = time.nowLocalDateTime.plusHours(24),
            location = "location"
        )

        // when & then
        assertThatThrownBy {
            workoutRequestService.saveChatWorkoutRequestMessage(
                chatRoom = chatRoom,
                fromMember = me,
                message = request
            )
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(ChatErrorCode.EXISTING_WORKOUT_REQUEST)
            })

    }


    @DisplayName("회원은 운동 요청 메시지를 보내는데 이미 운동 요청이 활성화되어있으면 전송을 하지 않는다.")
//    @Test
    fun saveChatWorkoutRequestMessageWhenExistsWorkoutRequest() {
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
        workoutRequest.updateStatus(WorkoutRequestStatus.PENDING)
        workoutRequestRepository.save(workoutRequest)

        val chatMessage2 = ChatMessage.ofWorkoutRequest(
            member = me,
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage2)

        val request = ChatWorkoutRequestMessageServiceRequest(
            scheduledAt = time.nowLocalDateTime.plusHours(5),
            location = "location"
        )

        // when & then
        assertThatThrownBy {
            workoutRequestService.saveChatWorkoutRequestMessage(
                fromMember = me,
                chatRoom = chatRoom,
                message = request
            )
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(ChatErrorCode.EXISTING_WORKOUT_REQUEST)
            })
    }



    @DisplayName("만료된 운동 요청을 전부 만료로 표시한다.")
    @Test
    fun modifyAllWorkoutRequestExpire() {
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

        val chatMessage = ChatMessage.ofWorkoutRequest(
            member = me,
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage)

        val workoutRequest1 = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        val expireWorkoutRequest1 = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime.minusHours(24)
        )
        val expireWorkoutRequest2 = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.minusHours(3),
            requestedAt = time.nowLocalDateTime
        )
        workoutRequestRepository.save(workoutRequest1)
        workoutRequestRepository.save(expireWorkoutRequest1)
        workoutRequestRepository.save(expireWorkoutRequest2)


        // when
        val response = workoutRequestService.modifyAllWorkoutRequestExpire()

        // then
        assertThat(response)
            .extracting("chatRoomId", "workoutRequestId", "status", "fromMemberId", "toMemberId")
            .containsExactlyInAnyOrder(
                tuple(chatRoom.id!!, expireWorkoutRequest1.id!!, WorkoutRequestStatus.EXPIRE, me.id!!, other.id!!),
                tuple(chatRoom.id!!, expireWorkoutRequest2.id!!, WorkoutRequestStatus.EXPIRE, me.id!!, other.id!!),
            )
    }

    @DisplayName("만료된 운동 요청을 전부 만료로 표시하면 실시간 알람을 보낸다.")
    @Test
    fun modifyAllWorkoutRequestExpireSendStomp() {
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

        val chatMessage = ChatMessage.ofWorkoutRequest(
            member = me,
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage)

        val workoutRequest1 = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        val expireWorkoutRequest1 = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime.minusHours(24)
        )
        val expireWorkoutRequest2 = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.minusHours(3),
            requestedAt = time.nowLocalDateTime
        )
        workoutRequestRepository.save(workoutRequest1)
        workoutRequestRepository.save(expireWorkoutRequest1)
        workoutRequestRepository.save(expireWorkoutRequest2)


        // when
        workoutRequestService.modifyAllWorkoutRequestExpire()

        // then
        val count = events.stream(StompEventUpdateWorkoutRequestMessageDepth1::class.java).count()
        assertThat(count).isEqualTo(4)
    }

    @DisplayName("운동 요청을 수락한다.")
    @Test
    fun modifyWorkoutRequest() {
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
        oAuth2Service.signup(signupRequest, other.id!!)

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

        val chatMessage = ChatMessage.ofWorkoutRequest(
            member = me,
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage)

        val workoutRequest = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        workoutRequestRepository.save(workoutRequest)

        val request = WorkoutRequestUpdateRequest(
            workoutRequestId = workoutRequest.id!!,
            status = WorkoutRequestStatusForRequest.ACCEPT
        )

        // when
        val updateWorkoutRequest = workoutRequestService.modifyWorkoutRequest(other.id!!, request)

        // then
        assertThat(updateWorkoutRequest)
            .extracting("chatRoom", "status", "fromMember", "toMember")
            .contains(
                chatRoom,
                WorkoutRequestStatus.ACCEPT,
                me,
                other
            )
    }

    @DisplayName("운동 요청 변경 시, 실시간 알람을 양측에 보낸다.")
    @Test
    fun modifyWorkoutRequestStomp() {
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
        oAuth2Service.signup(signupRequest, other.id!!)

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

        val chatMessage = ChatMessage.ofWorkoutRequest(
            member = me,
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage)

        val workoutRequest = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        workoutRequestRepository.save(workoutRequest)

        val request = WorkoutRequestUpdateRequest(
            workoutRequestId = workoutRequest.id!!,
            status = WorkoutRequestStatusForRequest.ACCEPT
        )

        // when
        workoutRequestService.modifyWorkoutRequest(other.id!!, request)

        // then
        val count = events.stream(StompEventUpdateWorkoutRequestMessageDepth1::class.java).count()
        assertThat(count).isEqualTo(2)
    }

    @DisplayName("운동 요청 수락 시, 인앱 알람을 양측에 보낸다.")
    @Test
    fun modifyWorkoutRequestAcceptNotification() {
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
        oAuth2Service.signup(signupRequest, other.id!!)

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

        val chatMessage = ChatMessage.ofWorkoutRequest(
            member = me,
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage)

        val workoutRequest = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        workoutRequestRepository.save(workoutRequest)

        val request = WorkoutRequestUpdateRequest(
            workoutRequestId = workoutRequest.id!!,
            status = WorkoutRequestStatusForRequest.ACCEPT
        )

        // when
        workoutRequestService.modifyWorkoutRequest(other.id!!, request)

        // then
        val count = events.stream(EventWorkoutRequestAccept::class.java).count()
        assertThat(count).isEqualTo(2)
    }

    @DisplayName("운동 요청의 상태를 완료로 바꾸면 운동 이력에 기록된다.")
    @Test
    fun modifyWorkoutRequestComplete() {
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
        oAuth2Service.signup(signupRequest, other.id!!)

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

        val chatMessage = ChatMessage.ofWorkoutRequest(
            member = me,
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage)

        val workoutRequest = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        workoutRequest.updateStatus(WorkoutRequestStatus.ACCEPT)
        workoutRequestRepository.save(workoutRequest)

        val request = WorkoutRequestUpdateRequest(
            workoutRequestId = workoutRequest.id!!,
            status = WorkoutRequestStatusForRequest.COMPLETE
        )

        // when
        val updateWorkoutRequest = workoutRequestService.modifyWorkoutRequest(other.id!!, request)

        // then
        assertThat(updateWorkoutRequest)
            .extracting("chatRoom", "status", "fromMember", "toMember")
            .contains(
                chatRoom,
                WorkoutRequestStatus.COMPLETE,
                me,
                other
            )

        val findWorkoutHistory = workoutHistoryRepository.findAll()[0]
        assertThat(findWorkoutHistory)
            .extracting("memberOne", "memberTwo")
            .contains(me, other)
    }

    @DisplayName("운동 요청이 수락되지 않으면 운동 요청의 상태를 완료로 바꿀 수 없다.")
    @Test
    fun modifyWorkoutRequestCompleteWhenNotAccept() {
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

        val chatMessage = ChatMessage.ofWorkoutRequest(
            member = me,
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage)

        val workoutRequest = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        workoutRequest.updateStatus(WorkoutRequestStatus.PENDING)
        workoutRequestRepository.save(workoutRequest)

        val request = WorkoutRequestUpdateRequest(
            workoutRequestId = workoutRequest.id!!,
            status = WorkoutRequestStatusForRequest.COMPLETE
        )

        // when & then
        assertThatThrownBy {
            workoutRequestService.modifyWorkoutRequest(other.id!!, request)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(WorkoutRequestErrorCode.CANNOT_COMPLETE_UNLESS_ACCEPTED)
            })
    }



    @ParameterizedTest(name = "이미 상태가 종료된 운동 요청에 대해서 상태를 변화시킬 수 없다.")
    @CsvSource("EXPIRE, ACCEPT", "REJECT, CANCEL", "COMPLETE")
    fun modifyWorkoutRequestEndedStatus(status: String) {
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

        val chatMessage = ChatMessage.ofWorkoutRequest(
            member = me,
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage)

        val workoutRequest = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        workoutRequest.updateStatus(WorkoutRequestStatus.valueOf(status))
        workoutRequestRepository.save(workoutRequest)

        val request = WorkoutRequestUpdateRequest(
            workoutRequestId = workoutRequest.id!!,
            status = WorkoutRequestStatusForRequest.ACCEPT
        )

        // when & then
        assertThatThrownBy {
            workoutRequestService.modifyWorkoutRequest(other.id!!, request)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(WorkoutRequestErrorCode.TERMINATED_WORKOUT_REQUEST_STATUS_CHANGE)
            })
    }


    @DisplayName("운동 요청을 한 사람이 거절을 할 수 없다.")
    @Test
    fun modifyWorkoutRequestFromMemberNotReject() {
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

        val chatMessage = ChatMessage.ofWorkoutRequest(
            member = me,
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage)

        val workoutRequest = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        workoutRequestRepository.save(workoutRequest)

        val request = WorkoutRequestUpdateRequest(
            workoutRequestId = workoutRequest.id!!,
            status = WorkoutRequestStatusForRequest.REJECT
        )

        // when & then
        assertThatThrownBy {
            workoutRequestService.modifyWorkoutRequest(me.id!!, request)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(WorkoutRequestErrorCode.FROM_MEMBER_CANNOT_REJECT)
            })

    }

    @DisplayName("운동 요청을 받은 사람이 취소 할 수 없다.")
    @Test
    fun modifyWorkoutRequestToMemberNotCancel() {
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

        val chatMessage = ChatMessage.ofWorkoutRequest(
            member = me,
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage)

        val workoutRequest = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        workoutRequestRepository.save(workoutRequest)

        val request = WorkoutRequestUpdateRequest(
            workoutRequestId = workoutRequest.id!!,
            status = WorkoutRequestStatusForRequest.CANCEL
        )

        // when & then
        assertThatThrownBy {
            workoutRequestService.modifyWorkoutRequest(other.id!!, request)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(WorkoutRequestErrorCode.TO_MEMBER_CANNOT_CANCEL)
            })
    }

    @DisplayName("운동 요청 상태 변경을 시도했으나, 변경 상태와 이미 기록된 상태가 같으면 요청을 거부한다.")
    @Test
    fun modifyWorkoutRequestDuplicateStatus() {
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

        val chatMessage = ChatMessage.ofWorkoutRequest(
            member = me,
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage)

        val workoutRequest = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        workoutRequest.updateStatus(WorkoutRequestStatus.ACCEPT)
        workoutRequestRepository.save(workoutRequest)

        val request = WorkoutRequestUpdateRequest(
            workoutRequestId = workoutRequest.id!!,
            status = WorkoutRequestStatusForRequest.ACCEPT
        )

        // when & then
        assertThatThrownBy {
            workoutRequestService.modifyWorkoutRequest(other.id!!, request)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(WorkoutRequestErrorCode.ALREADY_SAME_STATUS)
            })
    }

    @DisplayName("운동 요청을 보낸자가 수락을 할 수 없다.")
    @Test
    fun modifyWorkoutRequestNotSenderAccept() {
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

        val chatMessage = ChatMessage.ofWorkoutRequest(
            member = me,
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage)

        val workoutRequest = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        workoutRequestRepository.save(workoutRequest)

        val request = WorkoutRequestUpdateRequest(
            workoutRequestId = workoutRequest.id!!,
            status = WorkoutRequestStatusForRequest.ACCEPT
        )

        // when & then
        assertThatThrownBy {
            workoutRequestService.modifyWorkoutRequest(me.id!!, request)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(WorkoutRequestErrorCode.FROM_MEMBER_CANNOT_ACCEPT)
            })
    }

    @DisplayName("운동 요청 수락 시, 수락 안내 문구 생성")
    @Test
    fun modifyWorkoutRequestNoticeAccept() {
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
        oAuth2Service.signup(signupRequest, other.id!!)

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

        val chatMessage = ChatMessage.ofWorkoutRequest(
            member = me,
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage)

        val workoutRequest = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        workoutRequestRepository.save(workoutRequest)

        val request = WorkoutRequestUpdateRequest(
            workoutRequestId = workoutRequest.id!!,
            status = WorkoutRequestStatusForRequest.ACCEPT
        )

        // when
        workoutRequestService.modifyWorkoutRequest(other.id!!, request)

        // then
        val findNoticeMessages = chatNoticeMessageRepository.findAll()

        assertThat(findNoticeMessages).hasSize(1)
        assertThat(findNoticeMessages[0].type).isEqualTo(ChatNoticeMessageType.WORKOUT_REQUEST_ACCEPT)
    }

    @DisplayName("운동 완료 시, 후기 안내 문구 생성")
    @Test
    fun modifyWorkoutRequestNoticeComplete() {
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
        oAuth2Service.signup(signupRequest, other.id!!)

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

        val chatMessage = ChatMessage.ofWorkoutRequest(
            member = me,
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage)

        val workoutRequest = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        workoutRequest.updateStatus(WorkoutRequestStatus.ACCEPT)
        workoutRequestRepository.save(workoutRequest)

        val request = WorkoutRequestUpdateRequest(
            workoutRequestId = workoutRequest.id!!,
            status = WorkoutRequestStatusForRequest.COMPLETE
        )

        // when
        workoutRequestService.modifyWorkoutRequest(other.id!!, request)

        // then
        val findNoticeMessages = chatNoticeMessageRepository.findAll()

        assertThat(findNoticeMessages).hasSize(1)
        assertThat(findNoticeMessages[0].type).isEqualTo(ChatNoticeMessageType.WORKOUT_REQUEST_COMPLETE)
        assertThat(findNoticeMessages[0].workoutHistory).isNotNull()
    }

    @DisplayName("운동 요청 거절 시, 취소 안내 문구 생성")
    @Test
    fun modifyWorkoutRequestNoticeReject() {
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
        oAuth2Service.signup(signupRequest, other.id!!)

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

        val chatMessage = ChatMessage.ofWorkoutRequest(
            member = me,
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage)

        val workoutRequest = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        workoutRequestRepository.save(workoutRequest)

        val request = WorkoutRequestUpdateRequest(
            workoutRequestId = workoutRequest.id!!,
            status = WorkoutRequestStatusForRequest.REJECT
        )

        // when
        workoutRequestService.modifyWorkoutRequest(other.id!!, request)

        // then
        val findNoticeMessages = chatNoticeMessageRepository.findAll()

        assertThat(findNoticeMessages).hasSize(1)
        assertThat(findNoticeMessages[0].type).isEqualTo(ChatNoticeMessageType.WORKOUT_REQUEST_REJECT)
    }

    @DisplayName("운동 요청 취소 시, 취소 안내 문구 생성")
    @Test
    fun modifyWorkoutRequestNoticeCancel() {
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
        oAuth2Service.signup(signupRequest, other.id!!)

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

        val chatMessage = ChatMessage.ofWorkoutRequest(
            member = me,
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage)

        val workoutRequest = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        workoutRequestRepository.save(workoutRequest)

        val request = WorkoutRequestUpdateRequest(
            workoutRequestId = workoutRequest.id!!,
            status = WorkoutRequestStatusForRequest.CANCEL
        )

        // when
        workoutRequestService.modifyWorkoutRequest(me.id!!, request)

        // then
        val findNoticeMessages = chatNoticeMessageRepository.findAll()

        assertThat(findNoticeMessages).hasSize(1)
        assertThat(findNoticeMessages[0].type).isEqualTo(ChatNoticeMessageType.WORKOUT_REQUEST_CANCEL)
    }


}