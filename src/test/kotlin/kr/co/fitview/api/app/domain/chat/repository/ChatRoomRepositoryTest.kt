package kr.co.fitview.api.app.domain.chat.repository

import jakarta.persistence.EntityManager
import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.address.dto.request.AddressCreateServiceRequest
import kr.co.fitview.api.app.domain.address.entity.enums.AddressSiDo
import kr.co.fitview.api.app.domain.chat.condition.ChatCondition
import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
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
import kr.co.fitview.api.app.domain.workout.entity.WorkoutHistory
import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequest
import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import kr.co.fitview.api.app.domain.workout.repository.WorkoutHistoryRepository
import kr.co.fitview.api.app.domain.workout.repository.WorkoutRequestRepository
import kr.co.fitview.api.app.global.entity.Gender
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate
import java.time.ZoneId

class ChatRoomRepositoryTest @Autowired constructor(
    val chatRoomRepository: ChatRoomRepository,
    val chatParticipantRepository : ChatParticipantRepository,
    val chatMessageRepository : ChatMessageRepository,
    val workoutRequestRepository : WorkoutRequestRepository,
    val workoutHistoryRepository : WorkoutHistoryRepository,
    val oAuth2Service : OAuth2Service,
    val memberRepository : MemberRepository,
    val em : EntityManager,
    val time : Time
) : IntegrationTestSupport() {

    @DisplayName("두 회원이 참석한 개인 채팅방을 조회한다.")
    @Test
    fun findPrivateChatRoomIdBetweenMemberIds() {
        //given
        val member1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        val member2 = Member(
            email = "email2",
            password = "password2",
            role = Role.USER,
        )
        val savedMember1 = memberRepository.save(member1)
        val savedMember2 = memberRepository.save(member2)

        val savedChatRoom = chatRoomRepository.save(ChatRoom(ChatRoomType.PRIVATE))

        val chatParticipant1 = ChatParticipant(
            savedChatRoom,
            savedMember1
        )
        val chatParticipant2 = ChatParticipant(
            savedChatRoom,
            savedMember2
        )
        chatParticipantRepository.save(chatParticipant1)
        chatParticipantRepository.save(chatParticipant2)

        // when
        val findChatRoom = chatRoomRepository.findPrivateChatRoomIdBetweenMemberIds(
            member1.id!!, member2.id!!
        )

        // then
        assertThat(findChatRoom!!.id).isEqualTo(savedChatRoom.id)
    }


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
        intro: String = "intro",
        address : AddressCreateServiceRequest = AddressCreateServiceRequest(
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

    @DisplayName("회원이 참여한 채팅방 목록을 조회한다.")
    @Test
    fun findChatRoomProfileByDeletedAtIsNull() {
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
        chatRoom1.updateLastMessageAt(time.nowLocalDateTime)

        val chatRoom2 = chatRoomRepository.save(ChatRoom(ChatRoomType.PRIVATE))
        chatRoom2.updateLastMessageAt(time.nowLocalDateTime.minusHours(3))

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

        val condition = ChatCondition(
            size = 10,
            isCompleteWorkout = false
        )

        // when
        val response = chatRoomRepository.findChatRoomProfileByDeletedAtIsNull(me.id!!, condition)

        // then
        assertThat(response)
            .extracting("chatRoomId", "profileImageUrl", "nickname")
            .containsExactlyInAnyOrder(
                tuple(chatRoom1.id!!, signupRequest.profileImageUrl, other1.nickname),
                tuple(chatRoom2.id!!, signupRequest.profileImageUrl, other2.nickname),
            )
    }

    @DisplayName("회원이 참여한 채팅방 목록을 조회하되, 메시지 최신순으로 조회한다.")
    @Test
    fun findChatRoomProfileByDeletedAtIsNullOrderByMessagedAt() {
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

        val chatMessage1 = ChatMessage(
            member = me,
            chatRoom = chatRoom1,
            type = ChatMessageType.TEXT,
            content = "content",
            sentAt = time.nowLocalDateTime.minusHours(3)
        )
        chatRoom1.updateLastMessageAt(time.nowLocalDateTime.minusHours(3))

        val chatMessage2 = ChatMessage(
            member = me,
            chatRoom = chatRoom2,
            type = ChatMessageType.TEXT,
            content = "content",
            sentAt = time.nowLocalDateTime
        )
        chatRoom2.updateLastMessageAt(time.nowLocalDateTime)
        chatMessageRepository.save(chatMessage1)
        chatMessageRepository.save(chatMessage2)


        val condition = ChatCondition(
            size = 10,
            isCompleteWorkout = false
        )

        // when
        val response = chatRoomRepository.findChatRoomProfileByDeletedAtIsNull(me.id!!, condition).content

        // then
        assertThat(response[0])
            .extracting("chatRoomId", "profileImageUrl", "nickname")
            .contains(chatRoom2.id!!, signupRequest.profileImageUrl, other2.nickname)

        assertThat(response[1])
            .extracting("chatRoomId", "profileImageUrl", "nickname")
            .contains(chatRoom1.id!!, signupRequest.profileImageUrl, other1.nickname)
    }

    @DisplayName("채팅방 목록 조회 시, 최근 메시지 업데이트 시간이 주어지면 해당 시간보다 더 옛날 채팅방을 가져온다.")
    @Test
    fun findChatRoomProfileByDeletedAtIsNullExistsLastMessagedAt() {
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
        chatRoom1.updateLastMessageAt(time.nowLocalDateTime.minusHours(3))
        chatRoom2.updateLastMessageAt(time.nowLocalDateTime.minusHours(50))

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


        val condition = ChatCondition(
            size = 10,
            lastMessageAt = time.nowLocalDateTime.minusHours(3)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli(),
            isCompleteWorkout = false
        )

        // when
        val response = chatRoomRepository.findChatRoomProfileByDeletedAtIsNull(me.id!!, condition).content

        // then
        assertThat(response[0])
            .extracting("chatRoomId", "profileImageUrl", "nickname")
            .contains(chatRoom2.id!!, signupRequest.profileImageUrl, other2.nickname)

    }

    @DisplayName("운동 완료가 없는 채팅방을 조회한다.")
    @Test
    fun findChatRoomProfileByDeletedAtIsNullIsCompleteWorkoutFalse() {
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


        val chatMessage1 = ChatMessage(
            member = me,
            chatRoom = chatRoom1,
            type = ChatMessageType.TEXT,
            content = "content",
            sentAt = time.nowLocalDateTime.minusHours(3)
        )
        chatRoom1.updateLastMessageAt(time.nowLocalDateTime.minusHours(3))
        chatMessageRepository.save(chatMessage1)

        val chatMessage2 = ChatMessage(
            member = me,
            chatRoom = chatRoom2,
            type = ChatMessageType.WORKOUT_REQUEST,
            content = "content",
            sentAt = time.nowLocalDateTime
        )
        val workoutRequest = WorkoutRequest.of(
            chatMessage = chatMessage2,
            fromMember = me,
            toMember = other2,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        workoutRequest.updateStatus(WorkoutRequestStatus.COMPLETE)
        chatRoom2.updateLastMessageAt(time.nowLocalDateTime)
        chatMessageRepository.save(chatMessage2)
        workoutRequestRepository.save(workoutRequest)

         val workoutHistory = WorkoutHistory.of(
            chatRoom = chatRoom2,
            memberOne = me,
            memberTwo = other2
        )
        workoutHistoryRepository.save(workoutHistory)

        val condition = ChatCondition(
            size = 10,
            isCompleteWorkout = false
        )

        // when
        val response = chatRoomRepository.findChatRoomProfileByDeletedAtIsNull(me.id!!, condition)

        // then
        assertThat(response)
            .extracting("chatRoomId", "profileImageUrl", "nickname")
            .containsExactlyInAnyOrder(
                tuple(chatRoom1.id!!, signupRequest.profileImageUrl, other1.nickname),
            )
    }

    @DisplayName("운동 완료가 있는 채팅방을 조회한다.")
    @Test
    fun findChatRoomProfileByDeletedAtIsNullIsCompleteWorkoutTrue() {
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


        val chatMessage1 = ChatMessage(
            member = me,
            chatRoom = chatRoom1,
            type = ChatMessageType.TEXT,
            content = "content",
            sentAt = time.nowLocalDateTime.minusHours(3)
        )
        chatRoom1.updateLastMessageAt(time.nowLocalDateTime.minusHours(3))
        chatMessageRepository.save(chatMessage1)

        val chatMessage2 = ChatMessage(
            member = me,
            chatRoom = chatRoom2,
            type = ChatMessageType.WORKOUT_REQUEST,
            content = "content",
            sentAt = time.nowLocalDateTime
        )
        val workoutRequest = WorkoutRequest.of(
            chatMessage = chatMessage2,
            fromMember = me,
            toMember = other2,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        workoutRequest.updateStatus(WorkoutRequestStatus.COMPLETE)
        chatRoom2.updateLastMessageAt(time.nowLocalDateTime)
        chatMessageRepository.save(chatMessage2)
        workoutRequestRepository.save(workoutRequest)

        val workoutHistory1 = WorkoutHistory.of(
            chatRoom = chatRoom2,
            memberOne = me,
            memberTwo = other2
        )
        workoutHistoryRepository.save(workoutHistory1)

        val chatMessage3 = ChatMessage(
            member = me,
            chatRoom = chatRoom2,
            type = ChatMessageType.WORKOUT_REQUEST,
            content = "content",
            sentAt = time.nowLocalDateTime.plusHours(3)
        )
        val workoutRequest2 = WorkoutRequest.of(
            chatMessage = chatMessage3,
            fromMember = me,
            toMember = other2,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime.plusHours(3)
        )
        workoutRequest.updateStatus(WorkoutRequestStatus.COMPLETE)
        chatRoom2.updateLastMessageAt(time.nowLocalDateTime.plusHours(3))
        chatMessageRepository.save(chatMessage3)
        workoutRequestRepository.save(workoutRequest2)

        val workoutHistory2 = WorkoutHistory.of(
            chatRoom = chatRoom2,
            memberOne = me,
            memberTwo = other2
        )
        workoutHistoryRepository.save(workoutHistory2)

        val condition = ChatCondition(
            size = 10,
            isCompleteWorkout = true
        )

        // when
        val response = chatRoomRepository.findChatRoomProfileByDeletedAtIsNull(me.id!!, condition)

        // then
        assertThat(response)
            .extracting("chatRoomId", "profileImageUrl", "nickname")
            .containsExactlyInAnyOrder(
                tuple(chatRoom2.id!!, signupRequest.profileImageUrl, other2.nickname),
            )
    }

    @DisplayName("채팅방 조회 시, 채팅방 메시지 이력이 없으면 조회하지 않는다.")
    @Test
    fun findChatRoomProfileByDeletedAtIsNullNotSendMessage() {
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
        chatRoom1.updateLastMessageAt(time.nowLocalDateTime)

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

        val condition = ChatCondition(
            size = 10,
            isCompleteWorkout = false
        )

        em.flush()
        em.clear()

        // when
        val response = chatRoomRepository.findChatRoomProfileByDeletedAtIsNull(me.id!!, condition)

        // then
        assertThat(response).hasSize(1)
        assertThat(response)
            .extracting("chatRoomId", "profileImageUrl", "nickname")
            .containsExactlyInAnyOrder(
                tuple(chatRoom1.id!!, signupRequest.profileImageUrl, other1.nickname),
            )
    }

    @DisplayName("운동 요청이 있는 채팅방을 조회한다.")
    @Test
    fun findChatRoomByWorkoutRequestId() {
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
        memberRepository.save(me)
        memberRepository.save(other1)


        val chatRoom1 = chatRoomRepository.save(ChatRoom(ChatRoomType.PRIVATE))

        val chatParticipant1 = ChatParticipant(
            chatRoom1,
            me
        )
        val chatParticipant2 = ChatParticipant(
            chatRoom1,
            other1
        )

        chatParticipantRepository.save(chatParticipant1)
        chatParticipantRepository.save(chatParticipant2)


        val chatMessage1 = ChatMessage(
            member = me,
            chatRoom = chatRoom1,
            type = ChatMessageType.WORKOUT_REQUEST,
            content = "content",
            sentAt = time.nowLocalDateTime
        )
        val workoutRequest = WorkoutRequest.of(
            chatMessage = chatMessage1,
            fromMember = me,
            toMember = other1,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage1)
        workoutRequestRepository.save(workoutRequest)

        // when
        val findChatRoom = chatRoomRepository.findChatRoomByWorkoutRequestId(workoutRequest.id!!)

        // then
        assertThat(findChatRoom).isEqualTo(chatRoom1)
    }


}