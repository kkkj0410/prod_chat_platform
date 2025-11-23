package kr.co.fitview.api.app.domain.chat.repository

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.address.dto.request.AddressCreateServiceRequest
import kr.co.fitview.api.app.domain.address.entity.enums.AddressSiDo
import kr.co.fitview.api.app.domain.chat.condition.ChatCondition
import kr.co.fitview.api.app.domain.chat.entity.*
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle
import kr.co.fitview.api.app.domain.member.entity.enums.WorkoutTimeName
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.oauth2.dto.request.OAuth2SignupServiceRequest
import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.domain.workout.dto.response.enums.WorkoutRequestStatusForResponse
import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequest
import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import kr.co.fitview.api.app.domain.workout.repository.WorkoutRequestRepository
import kr.co.fitview.api.app.global.entity.Gender
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate
import java.time.ZoneId

class ChatMessageRepositoryTest @Autowired constructor(
    val chatMessageRepository: ChatMessageRepository,
    val chatParticipantRepository: ChatParticipantRepository,
    val chatRoomRepository: ChatRoomRepository,
    val workoutRequestRepository: WorkoutRequestRepository,
    val messageReadStatusRepository: MessageReadStatusRepository,
    val oAuth2Service: OAuth2Service,
    val memberRepository: MemberRepository,
    val time: Time
) : IntegrationTestSupport() {


    fun createOAuth2SignupServiceRequest(
        profileImageUrl: String = "profileImageUrl",
        nickname: String = "nickname",
        gender: Gender = Gender.MALE,
        birthday: LocalDate = LocalDate.of(2000, 1, 1),
        height: Int = 170,
        weight: Int = 65,
        workoutExperience: MemberWorkoutExperience = MemberWorkoutExperience.JUST_STARTED,
        workoutStyle: MemberWorkoutStyle = MemberWorkoutStyle.STRENGTH,
        workoutTimes: List<WorkoutTimeName> = listOf(WorkoutTimeName.WEEKDAY_DAWN, WorkoutTimeName.WEEKDAY_EVENING),
        workoutGoal: MemberWorkoutGoal = MemberWorkoutGoal.PERFORMANCE_GOAL,
        workoutImageUrls: List<String> = listOf(
            "imageUrl1",
            "imageUrl2",
        ),
        intro: String? = "intro",
        address: AddressCreateServiceRequest = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "강남구",
            eupMyeonDong = "역삼동",
            lat = 37.4979,
            lng = 127.0276,
            fullAddress = "서울특별시 강남구 테헤란로 123"
        )
    ): OAuth2SignupServiceRequest {
        return OAuth2SignupServiceRequest(
            profileImageUrl = profileImageUrl,
            nickname = nickname,
            gender = gender,
            birthday = birthday,
            height = height,
            weight = weight,
            workoutExperience = workoutExperience,
            workoutStyle = workoutStyle,
            workoutTimes = workoutTimes,
            workoutGoal = workoutGoal,
            workoutImageUrls = workoutImageUrls,
            intro = intro,
            address = address
        )
    }


    @DisplayName("각 채팅방의 최근 메시지를 조회한다.")
    @Test
    fun findRecentChatMessageByMemberIdAndIn() {
        //given
        val me = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        val other1 = Member(
            email = "email2",
            password = "password2",
            role = Role.USER,
        )
        val other2 = Member(
            email = "email2",
            password = "password2",
            role = Role.USER,
        )
        memberRepository.save(me)
        memberRepository.save(other1)
        memberRepository.save(other2)

        val signupRequest = createOAuth2SignupServiceRequest()

        oAuth2Service.signup(signupRequest, me.id!!)
        oAuth2Service.signup(signupRequest, other1.id!!)
        oAuth2Service.signup(signupRequest, other2.id!!)

        val chatRoom1 = chatRoomRepository.save(ChatRoom(ChatRoomType.PRIVATE))
        val chatRoom2 = chatRoomRepository.save(ChatRoom(ChatRoomType.PRIVATE))

        val chatParticipant1 = ChatParticipant(
            chatRoom1,
            me
        )
        val chatParticipant2 = ChatParticipant(
            chatRoom1,
            other1
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

        val message1ByChatRoom1 = ChatMessage(
            member = me,
            chatRoom = chatRoom1,
            type = ChatMessageType.TEXT,
            content = "content",
            sentAt = time.nowLocalDateTime.minusHours(3)
        )
        val message2ByChatRoom1 = ChatMessage(
            member = me,
            chatRoom = chatRoom1,
            type = ChatMessageType.TEXT,
            content = "content",
            sentAt = time.nowLocalDateTime
        )
        chatRoom1.updateLastMessageAt(time.nowLocalDateTime)

        val message1ByChatRoom2 = ChatMessage(
            member = other2,
            chatRoom = chatRoom2,
            type = ChatMessageType.WORKOUT_REQUEST,
            sentAt = time.nowLocalDateTime.minusHours(3)
        )
        val workout1ByChatRoom2 = WorkoutRequest(
            chatMessage = message1ByChatRoom2,
            fromMember = other2,
            toMember = me,
            status = WorkoutRequestStatus.PENDING,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusHours(24),
            requestedAt = time.nowLocalDateTime.minusHours(3)
        )
        chatRoom2.updateLastMessageAt(time.nowLocalDateTime.minusHours(3))

        chatMessageRepository.save(message1ByChatRoom1)
        chatMessageRepository.save(message2ByChatRoom1)
        chatMessageRepository.save(message1ByChatRoom2)

        workoutRequestRepository.save(workout1ByChatRoom2)

        val messageRead = MessageReadStatus(
            chatRoom = chatRoom2,
            member = me,
            chatMessage = message1ByChatRoom2,
            isRead = true
        )
        messageReadStatusRepository.save(messageRead)

        val chatRoomIds = listOf(chatRoom1.id!!, chatRoom2.id!!)

        // when
        val response = chatMessageRepository.findRecentChatMessageByMemberIdAndIn(me.id!!, chatRoomIds)

        assertThat(response[0])
            .extracting(
                "chatMessageId",
                "type",
                "sentAt",
                "isMe",
                "isRead",
                "chatRoomId",
                "memberId",
                "content"
            )
            .contains(
                message2ByChatRoom1.id!!,
                ChatMessageType.TEXT,
                time.nowLocalDateTime,
                true,
                true,
                chatRoom1.id!!,
                me.id!!,
                message2ByChatRoom1.content
            )

        assertThat(response[1])
            .extracting(
                "chatMessageId",
                "type",
                "sentAt",
                "isMe",
                "isRead",
                "chatRoomId",
                "memberId",
                "workoutRequestId",
                "status",
                "scheduledAt",
                "location"
            )
            .contains(
                message1ByChatRoom2.id!!,
                ChatMessageType.WORKOUT_REQUEST,
                message1ByChatRoom2.sentAt,
                false,
                true,
                chatRoom2.id!!,
                other2.id!!,
                workout1ByChatRoom2.id!!,
                WorkoutRequestStatusForResponse.PENDING,
                time.nowLocalDateTime.plusHours(24),
                "location"
            )
    }

    @DisplayName("채팅방의 메시지를 조회한다.")
    @Test
    fun findChatMessageByCondition() {
        //given
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


        val chatMessage1 = ChatMessage(
            member = me,
            chatRoom = chatRoom,
            type = ChatMessageType.TEXT,
            content = "content",
            sentAt = time.nowLocalDateTime.minusHours(20)
        )
        chatMessageRepository.save(chatMessage1)

        val chatMessage2 = ChatMessage(
            member = me,
            chatRoom = chatRoom,
            type = ChatMessageType.WORKOUT_REQUEST,
            sentAt = time.nowLocalDateTime.minusHours(10)
        )
        val workoutRequest = WorkoutRequest(
            chatMessage = chatMessage2,
            fromMember = other,
            toMember = me,
            status = WorkoutRequestStatus.PENDING,
            scheduledAt = time.nowLocalDateTime.plusHours(24),
            requestedAt = time.nowLocalDateTime.minusHours(10),
            location = "location"
        )
        chatMessageRepository.save(chatMessage2)
        workoutRequestRepository.save(workoutRequest)

        val condition = ChatCondition(
            size = 10,
        )

        // when
        val chatMessageAndWorkoutRequests = chatMessageRepository.findChatMessageByCondition(chatRoom.id!!, condition)
        val response = chatMessageAndWorkoutRequests.content

        // then
        assertThat(response[0].chatMessage).isEqualTo(chatMessage2)
        assertThat(response[0].workoutRequest).isEqualTo(workoutRequest)

        assertThat(response[1].chatMessage).isEqualTo(chatMessage1)
        assertThat(response[1].workoutRequest).isNull()
    }


    @DisplayName("채팅방의 메시지를 조회한다. 최근 메시지를 읽은 기록이 있다면 해당 이후의 더 옛날 메시지를 조회한다.")
    @Test
    fun findChatMessageByConditionExistsCursorAt() {
        //given
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


        val chatMessage1 = ChatMessage(
            member = me,
            chatRoom = chatRoom,
            type = ChatMessageType.TEXT,
            content = "content",
            sentAt = time.nowLocalDateTime.minusHours(20)
        )
        chatMessageRepository.save(chatMessage1)

        val chatMessage2 = ChatMessage(
            member = me,
            chatRoom = chatRoom,
            type = ChatMessageType.WORKOUT_REQUEST,
            sentAt = time.nowLocalDateTime.minusHours(10)
        )
        val workoutRequest = WorkoutRequest(
            chatMessage = chatMessage2,
            fromMember = other,
            toMember = me,
            status = WorkoutRequestStatus.PENDING,
            scheduledAt = time.nowLocalDateTime.plusHours(24),
            requestedAt = time.nowLocalDateTime.minusHours(10),
            location = "location"
        )
        chatMessageRepository.save(chatMessage2)
        workoutRequestRepository.save(workoutRequest)

        val condition = ChatCondition(
            size = 10,
            lastMessageAt = time.nowLocalDateTime.minusHours(10)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        )

        // when
        val chatMessageAndWorkoutRequests = chatMessageRepository.findChatMessageByCondition(chatRoom.id!!, condition)
        val response = chatMessageAndWorkoutRequests.content

        // then
        assertThat(response).hasSize(1)
        assertThat(response[0].chatMessage).isEqualTo(chatMessage1)
        assertThat(response[0].workoutRequest).isNull()
    }
}