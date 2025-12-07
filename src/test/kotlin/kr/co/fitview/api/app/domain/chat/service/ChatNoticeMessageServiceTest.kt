package kr.co.fitview.api.app.domain.chat.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.dto.ExpireWorkoutRequest
import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatNoticeMessageType
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.chat.repository.*
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.notification.dto.response.StompEventChatNoticeMessageDepth1
import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.domain.workout_history.entity.WorkoutHistory
import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequest
import kr.co.fitview.api.app.domain.workout_history.repository.WorkoutHistoryRepository
import kr.co.fitview.api.app.domain.workout.repository.WorkoutRequestRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import kr.co.fitview.api.app.global.util.TestDataFactory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
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
    private val workoutHistoryRepository: WorkoutHistoryRepository,
    private val time : Time,
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

    @DisplayName("안내 문구 유형의 채팅 메시지를 저장 시, 채팅방 최근 문자 시간을 갱신한다.")
    @Test
    fun addChatNoticeFromLastMessageAt() {
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
        chatNoticeMessageService.addChatNoticeFrom(
            me.id!!,
            workoutRequestId = workoutRequest.id!!,
            type = ChatNoticeMessageType.WORKOUT_REQUEST_COMPLETE
        )

        // then
        assertThat(chatRoom.lastMessageAt).isEqualTo(time.nowLocalDateTime)
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
        chatNoticeMessageService.addChatNoticeFrom(
            me.id!!,
            workoutRequestId = workoutRequest.id!!,
            type = ChatNoticeMessageType.WORKOUT_REQUEST_ACCEPT
        )


        // then
        val count = events.stream(StompEventChatNoticeMessageDepth1::class.java).count()
        assertThat(count).isEqualTo(2)
    }

    @DisplayName("운동 요청 만료 안내 문구를 생성한다.")
    @Test
    fun addAllExpireChatNoticeFrom() {
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
        val other2 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(me)
        memberRepository.save(other)
        memberRepository.save(other2)

        val signupRequest = TestDataFactory.oAuth2SignupRequest()
        oAuth2Service.signup(signupRequest, me.id!!)
        oAuth2Service.signup(signupRequest, other.id!!)
        oAuth2Service.signup(signupRequest, other2.id!!)

        val chatRoom = chatRoomRepository.save(ChatRoom(ChatRoomType.PRIVATE))
        val chatRoom2 = chatRoomRepository.save(ChatRoom(ChatRoomType.PRIVATE))

        val chatParticipant1 = ChatParticipant(
            chatRoom,
            me
        )
        val chatParticipant2 = ChatParticipant(
            chatRoom,
            other
        )
        val chatParticipant3 = ChatParticipant(
            chatRoom2,
            me
        )
        val chatParticipant4 = ChatParticipant(
            chatRoom2,
            other2
        )
        chatParticipantRepository.save(chatParticipant1)
        chatParticipantRepository.save(chatParticipant2)
        chatParticipantRepository.save(chatParticipant3)
        chatParticipantRepository.save(chatParticipant4)

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

        val chatMessage2 = ChatMessage.ofText(
            member = me,
            chatRoom = chatRoom2,
            content = "content",
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage2)

        val request = listOf(
            ExpireWorkoutRequest(
                memberOneId = me.id!!,
                memberTwoId = other.id!!,
                chatRoomId = chatRoom.id!!
            ),
            ExpireWorkoutRequest(
                memberOneId = me.id!!,
                memberTwoId = other2.id!!,
                chatRoomId = chatRoom2.id!!
            )
        )

        // when
        chatNoticeMessageService.addAllExpireChatNoticeFrom(request)

        // then
        val findChatMessages = chatMessageRepository.findByType(ChatMessageType.NOTICE)
        assertThat(findChatMessages).hasSize(2)

        val findChatNoticeMessages = chatNoticeMessageRepository.findAll()
        assertThat(findChatNoticeMessages).hasSize(2)
        assertThat(findChatNoticeMessages[0].type).isEqualTo(ChatNoticeMessageType.WORKOUT_REQUEST_EXPIRE)
        assertThat(findChatNoticeMessages[1].type).isEqualTo(ChatNoticeMessageType.WORKOUT_REQUEST_EXPIRE)
    }

    @DisplayName("운동 요청 만료 안내 문구 생성 시, 해당 문구의 메시지 읽음 여부도 생성한다..")
    @Test
    fun addAllExpireChatNoticeFromMessageRead() {
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
        val other2 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(me)
        memberRepository.save(other)
        memberRepository.save(other2)

        val signupRequest = TestDataFactory.oAuth2SignupRequest()
        oAuth2Service.signup(signupRequest, me.id!!)
        oAuth2Service.signup(signupRequest, other.id!!)
        oAuth2Service.signup(signupRequest, other2.id!!)

        val chatRoom = chatRoomRepository.save(ChatRoom(ChatRoomType.PRIVATE))
        val chatRoom2 = chatRoomRepository.save(ChatRoom(ChatRoomType.PRIVATE))

        val chatParticipant1 = ChatParticipant(
            chatRoom,
            me
        )
        val chatParticipant2 = ChatParticipant(
            chatRoom,
            other
        )
        val chatParticipant3 = ChatParticipant(
            chatRoom2,
            me
        )
        val chatParticipant4 = ChatParticipant(
            chatRoom2,
            other2
        )
        chatParticipantRepository.save(chatParticipant1)
        chatParticipantRepository.save(chatParticipant2)
        chatParticipantRepository.save(chatParticipant3)
        chatParticipantRepository.save(chatParticipant4)

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

        val chatMessage2 = ChatMessage.ofText(
            member = me,
            chatRoom = chatRoom2,
            content = "content",
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage2)

        val request = listOf(
            ExpireWorkoutRequest(
                memberOneId = me.id!!,
                memberTwoId = other.id!!,
                chatRoomId = chatRoom.id!!
            ),
            ExpireWorkoutRequest(
                memberOneId = me.id!!,
                memberTwoId = other2.id!!,
                chatRoomId = chatRoom2.id!!
            )
        )

        // when
        chatNoticeMessageService.addAllExpireChatNoticeFrom(request)

        // then
        val findMessageReadStatuses = messageReadStatusRepository.findAll()
        assertThat(findMessageReadStatuses).hasSize(4)

        assertThat(findMessageReadStatuses)
            .extracting("chatRoom", "member")
            .containsExactlyInAnyOrder(
                tuple(chatRoom, me),
                tuple(chatRoom, other),
                tuple(chatRoom2, me),
                tuple(chatRoom2, other2),
            )
    }

    @DisplayName("운동 요청 만료 안내 문구 생성 시, 각 회원에게 실시간 알람을 보낸다.")
    @Test
    fun addAllExpireChatNoticeFromSendStomp() {
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
        val other2 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(me)
        memberRepository.save(other)
        memberRepository.save(other2)

        val signupRequest = TestDataFactory.oAuth2SignupRequest()
        oAuth2Service.signup(signupRequest, me.id!!)

        val signupRequest2 = TestDataFactory.oAuth2SignupRequest(
            profileImageUrl = "updateImage1",
            nickname = "update1"
        )
        oAuth2Service.signup(signupRequest2, other.id!!)

        val signupRequest3 = TestDataFactory.oAuth2SignupRequest(
            profileImageUrl = "updateImage2",
            nickname = "update2"
        )
        oAuth2Service.signup(signupRequest3, other2.id!!)

        val chatRoom = chatRoomRepository.save(ChatRoom(ChatRoomType.PRIVATE))
        val chatRoom2 = chatRoomRepository.save(ChatRoom(ChatRoomType.PRIVATE))

        val chatParticipant1 = ChatParticipant(
            chatRoom,
            me
        )
        val chatParticipant2 = ChatParticipant(
            chatRoom,
            other
        )
        val chatParticipant3 = ChatParticipant(
            chatRoom2,
            me
        )
        val chatParticipant4 = ChatParticipant(
            chatRoom2,
            other2
        )
        chatParticipantRepository.save(chatParticipant1)
        chatParticipantRepository.save(chatParticipant2)
        chatParticipantRepository.save(chatParticipant3)
        chatParticipantRepository.save(chatParticipant4)

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

        val chatMessage2 = ChatMessage.ofText(
            member = me,
            chatRoom = chatRoom2,
            content = "content",
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage2)

        val workoutHistory = WorkoutHistory(
            chatRoom = chatRoom2,
            memberOne = me,
            memberTwo = other2,
            completedAt = time.nowLocalDateTime
        )
        workoutHistoryRepository.save(workoutHistory)

        val request = listOf(
            ExpireWorkoutRequest(
                memberOneId = me.id!!,
                memberTwoId = other.id!!,
                chatRoomId = chatRoom.id!!
            ),
            ExpireWorkoutRequest(
                memberOneId = me.id!!,
                memberTwoId = other2.id!!,
                chatRoomId = chatRoom2.id!!
            )
        )

        // when
        chatNoticeMessageService.addAllExpireChatNoticeFrom(request)

        // then
        val count = events.stream(StompEventChatNoticeMessageDepth1::class.java).count()
        assertThat(count).isEqualTo(4)
    }

    @DisplayName("운동 요청 만료 안내 문구를 생성 시, 채팅방의 최근 시간이 새로 갱신된다..")
    @Test
    fun addAllExpireChatNoticeFromLastMessageAt() {
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
        val other2 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(me)
        memberRepository.save(other)
        memberRepository.save(other2)

        val signupRequest = TestDataFactory.oAuth2SignupRequest()
        oAuth2Service.signup(signupRequest, me.id!!)
        oAuth2Service.signup(signupRequest, other.id!!)
        oAuth2Service.signup(signupRequest, other2.id!!)

        val chatRoom = chatRoomRepository.save(ChatRoom(ChatRoomType.PRIVATE))
        val chatRoom2 = chatRoomRepository.save(ChatRoom(ChatRoomType.PRIVATE))

        val chatParticipant1 = ChatParticipant(
            chatRoom,
            me
        )
        val chatParticipant2 = ChatParticipant(
            chatRoom,
            other
        )
        val chatParticipant3 = ChatParticipant(
            chatRoom2,
            me
        )
        val chatParticipant4 = ChatParticipant(
            chatRoom2,
            other2
        )
        chatParticipantRepository.save(chatParticipant1)
        chatParticipantRepository.save(chatParticipant2)
        chatParticipantRepository.save(chatParticipant3)
        chatParticipantRepository.save(chatParticipant4)

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

        val chatMessage2 = ChatMessage.ofText(
            member = me,
            chatRoom = chatRoom2,
            content = "content",
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage2)

        val request = listOf(
            ExpireWorkoutRequest(
                memberOneId = me.id!!,
                memberTwoId = other.id!!,
                chatRoomId = chatRoom.id!!
            ),
            ExpireWorkoutRequest(
                memberOneId = me.id!!,
                memberTwoId = other2.id!!,
                chatRoomId = chatRoom2.id!!
            )
        )

        // when
        chatNoticeMessageService.addAllExpireChatNoticeFrom(request)

        // then
        assertThat(chatRoom.lastMessageAt).isEqualTo(time.nowLocalDateTime)
    }
}