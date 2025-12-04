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
import kr.co.fitview.api.app.domain.chat.repository.ChatMessageRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatNoticeMessageRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatParticipantRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatRoomRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.domain.workout.dto.response.enums.WorkoutRequestStatusForRequest
import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequest
import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import kr.co.fitview.api.app.domain.workout.repository.WorkoutHistoryRepository
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
    val time : Time
) : IntegrationTestSupport() {

    @DisplayName("운동 요청을 추가한다.")
    @Test
    fun addWorkoutRequest() {
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


        val request = ChatWorkoutRequestMessageServiceRequest(
            type = ChatMessageType.WORKOUT_REQUEST,
            scheduledAt = time.nowLocalDateTime.plusHours(24),
            location = "location"
        )

        // when
        workoutRequestService.addWorkoutRequest(
            chatRoomId = chatRoom.id!!,
            chatMessage = chatMessage,
            fromMember = me,
            toMember = other,
            message = request
        )

        // then
        val findWorkoutRequest = workoutRequestRepository.findAll()[0]
        assertThat(findWorkoutRequest)
            .extracting(
                "chatMessage",
                "fromMember",
                "toMember",
                "status",
                "location",
                "scheduledAt",
                "requestedAt")
            .contains(
                chatMessage,
                me,
                other,
                WorkoutRequestStatus.PENDING,
                "location",
                time.nowLocalDateTime.plusHours(24),
                time.nowLocalDateTime
            )
    }

    @DisplayName("운동 요청이 이미 존재하지만 끝난 상태이면 다시 운동 요청 가능")
    @ParameterizedTest(name = "case {index}: 기존 요청 상태 = {0}")
    @CsvSource("REJECT", "CANCEL", "COMPLETE", "EXPIRE")
    fun addWorkoutRequestWhenExistsEndedWorkoutRequest(status : String) {
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
        val savedWorkoutRequest = workoutRequestService.addWorkoutRequest(
            chatRoomId = chatRoom.id!!,
            chatMessage = chatMessage2,
            fromMember = me,
            toMember = other,
            message = request
        )

        // then
        assertThat(savedWorkoutRequest.id).isNotNull()
        assertThat(savedWorkoutRequest)
            .extracting(
                "chatMessage",
                "fromMember",
                "toMember",
                "status",
                "location",
                "scheduledAt",
                "requestedAt")
            .contains(
                chatMessage2,
                me,
                other,
                WorkoutRequestStatus.PENDING,
                "location",
                time.nowLocalDateTime.plusHours(24),
                time.nowLocalDateTime
            )

    }


    @DisplayName("운동 요청이 이미 존재하면 운동 요청을 보낼 수 없다")
//    @ParameterizedTest(name = "case {index}: 기존 요청 상태 = {0}")
    @CsvSource("PENDING", "ACCEPT",)
    fun addWorkoutRequestWhenExistsWorkoutRequest(status : String) {
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
            workoutRequestService.addWorkoutRequest(
                chatRoomId = chatRoom.id!!,
                chatMessage = chatMessage2,
                fromMember = me,
                toMember = other,
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
        val response = workoutRequestService.modifyWorkoutRequest(other.id!!, request)

        // then
        assertThat(response.status).isEqualTo(WorkoutRequestStatus.ACCEPT)
        assertThat(response)
            .extracting("chatRoomId", "workoutRequestId", "status", "fromMemberId", "toMemberId")
            .contains(
                chatRoom.id!!,
                workoutRequest.id!!,
                WorkoutRequestStatus.ACCEPT,
                me.id!!,
                other.id!!
            )
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
        val response = workoutRequestService.modifyWorkoutRequest(other.id!!, request)

        // then
        assertThat(response.status).isEqualTo(WorkoutRequestStatus.COMPLETE)
        assertThat(response)
            .extracting("chatRoomId", "workoutRequestId", "status", "fromMemberId", "toMemberId")
            .contains(
                chatRoom.id!!,
                workoutRequest.id!!,
                WorkoutRequestStatus.COMPLETE,
                me.id!!,
                other.id!!
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



    @DisplayName("채팅방의 제일 최큰 운동 요청을 확인한다.")
    @Test
    fun findRecentWorkoutRequestFrom() {
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

        // when
        val findWorkoutRequest = workoutRequestService.findRecentWorkoutRequestFrom(chatRoom.id!!)

        // then
        assertThat(findWorkoutRequest)
            .extracting("chatMessage", "status")
            .contains(
                chatMessage,
                WorkoutRequestStatus.PENDING
            )
    }

    @DisplayName("채팅방의 제일 최큰 운동 요청을 조회하되, 운동 요청이 아예 없으면 조회하지 않는다.")
    @Test
    fun findRecentWorkoutRequestFromWhenNull() {
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

        // when
        val findWorkoutRequest = workoutRequestService.findRecentWorkoutRequestFrom(chatRoom.id!!)

        // then
        assertThat(findWorkoutRequest).isNull()
    }

}