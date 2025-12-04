package kr.co.fitview.api.app.domain.chat.service

import jakarta.persistence.Column
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.validation.constraints.NotNull
import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.dto.response.ChatMessageDetailResponse
import kr.co.fitview.api.app.domain.chat.dto.response.StompChatNoticeMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.QChatMessage.chatMessage
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatNoticeMessageType
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.chat.repository.*
import kr.co.fitview.api.app.domain.member.dto.response.MemberChatProfileResponse
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.notification.constant.StompConstant
import kr.co.fitview.api.app.domain.notification.dto.StompSendEvent
import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequest
import kr.co.fitview.api.app.domain.workout.repository.WorkoutRequestRepository
import kr.co.fitview.api.app.global.dto.WsMessageType
import kr.co.fitview.api.app.global.dto.WsResponse
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import kr.co.fitview.api.app.global.util.TestDataFactory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.hibernate.annotations.ColumnDefault
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.then
import org.springframework.beans.factory.annotation.Autowired

class ChatNoticeMessageServiceTest @Autowired constructor(
    private val chatNoticeMessageService: ChatNoticeMessageService,
    private val memberRepository : MemberRepository,
    private val chatRoomRepository : ChatRoomRepository,
    private val chatParticipantRepository : ChatParticipantRepository,
    private val chatMessageRepository : ChatMessageRepository,
    private val workoutRequestRepository : WorkoutRequestRepository,
    private val chatNoticeMessageRepository : ChatNoticeMessageRepository,
    private val oAuth2Service : OAuth2Service,
    private val messageReadStatusRepository : MessageReadStatusRepository,
    private val time : Time
) : IntegrationTestSupport(){


    @DisplayName("안내 문구 유형의 채팅 메시지를 저장한다.")
    @Test
    fun addChatNoticeFrom() {
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


        // when
        val savedChatMessage = chatNoticeMessageService.addChatNoticeFrom(
            me.id!!,
            workoutRequestId = workoutRequest.id!!,
            type = ChatNoticeMessageType.WORKOUT_REQUEST_COMPLETE
        )

        // then
        assertThat(savedChatMessage.id).isNotNull()

        val findChatNoticeMessages = chatNoticeMessageRepository.findAll()
        assertThat(findChatNoticeMessages).hasSize(1)
        assertThat(findChatNoticeMessages[0].type).isEqualTo(ChatNoticeMessageType.WORKOUT_REQUEST_COMPLETE)

    }

    @DisplayName("안내 문구 유형의 채팅 메시지를 저장 시, 읽음 여부도 저장한다..")
    @Test
    fun addChatNoticeFromWithMessageReadStatus() {
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


        // when
        val savedChatMessage = chatNoticeMessageService.addChatNoticeFrom(
            me.id!!,
            workoutRequestId = workoutRequest.id!!,
            type = ChatNoticeMessageType.WORKOUT_REQUEST_COMPLETE
        )

        // then
        val findMessageReadStatuses = messageReadStatusRepository.findAll()
        assertThat(findMessageReadStatuses).hasSize(2)

        assertThat(findMessageReadStatuses)
            .extracting("chatRoom", "member", "chatMessage")
            .containsExactlyInAnyOrder(
                tuple(chatRoom, me, savedChatMessage),
                tuple(chatRoom, other, savedChatMessage),
            )

    }

    @DisplayName("안내 문구 유형의 채팅 메시지 저장 시, 실시간 알람을 보낸다.")
    @Test
    fun addChatNoticeFromWithNotification() {
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


        // when
        val savedChatMessage = chatNoticeMessageService.addChatNoticeFrom(
            me.id!!,
            workoutRequestId = workoutRequest.id!!,
            type = ChatNoticeMessageType.WORKOUT_REQUEST_ACCEPT
        )


        // then
        val chatMessage = StompChatNoticeMessage(
            chatMessageId = savedChatMessage.id!!,
            sentAt = time.nowLocalDateTime,
            content = ChatNoticeMessageType.WORKOUT_REQUEST_ACCEPT,
            workoutHistoryId = null
        )

        val chatProfile = MemberChatProfileResponse(
            profileImageUrl = signupRequest.profileImageUrl,
            nickname = signupRequest.nickname
        )

        val response = ChatMessageDetailResponse.of(
            chatRoomId = chatRoom.id!!,
            isCompleteWorkout = false,
            chatProfile = chatProfile,
            chatMessage = chatMessage,
            otherMemberId = other.id!!
        )

        then(stompPublisher).should().sendToUser(
            StompSendEvent(
                memberId = me.id!!,
                destination = StompConstant.SUB_CHAT_MESSAGE,
                payload = WsResponse(
                    type = WsMessageType.NOTICE.code,
                    payload = response
                )
            )
        )
        then(stompPublisher).should().sendToUser(
            StompSendEvent(
                memberId = other.id!!,
                destination = StompConstant.SUB_CHAT_MESSAGE,
                payload = WsResponse(
                    type = WsMessageType.NOTICE.code,
                    payload = response
                )
            )
        )
    }
}