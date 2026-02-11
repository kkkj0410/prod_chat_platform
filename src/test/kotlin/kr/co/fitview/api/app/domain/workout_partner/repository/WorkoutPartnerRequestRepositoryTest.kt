package kr.co.fitview.api.app.domain.workout_partner.repository

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.address.dto.request.AddressCreateServiceRequest
import kr.co.fitview.api.app.domain.address.entity.enums.AddressSiDo
import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.chat.repository.ChatMessageRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatParticipantRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatRoomRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle
import kr.co.fitview.api.app.domain.member.entity.enums.WorkoutTimeName
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.member.service.MemberService
import kr.co.fitview.api.app.domain.oauth2.dto.request.OAuth2SignupServiceRequest
import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequest
import kr.co.fitview.api.app.domain.workout.repository.WorkoutRequestRepository
import kr.co.fitview.api.app.domain.workout_history.entity.WorkoutHistory
import kr.co.fitview.api.app.domain.workout_history.repository.WorkoutHistoryRepository
import kr.co.fitview.api.app.domain.workout_partner.condition.AdminWorkoutPartnerRequestCondition
import kr.co.fitview.api.app.domain.workout_partner.condition.WorkoutPartnerRequestCondition
import kr.co.fitview.api.app.domain.workout_partner.dto.request.enums.WorkoutPartnerRequestType
import kr.co.fitview.api.app.domain.workout_partner.dto.response.enums.WorkoutPartnerRequestStatusForResponse
import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartner
import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartnerRequest
import kr.co.fitview.api.app.domain.workout_partner.entity.enums.WorkoutPartnerRequestContent
import kr.co.fitview.api.app.domain.workout_partner.entity.enums.WorkoutPartnerRequestStatus
import kr.co.fitview.api.app.global.entity.Gender
import kr.co.fitview.api.app.global.entity.OAuth2Provider
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate

class WorkoutPartnerRequestRepositoryTest @Autowired constructor(
    val workoutPartnerRequestRepository : WorkoutPartnerRequestRepository,
    val workoutPartnerRepository : WorkoutPartnerRepository,
    val memberService : MemberService,
    val memberRepository : MemberRepository,
    val chatRoomRepository : ChatRoomRepository,
    val chatMessageRepository : ChatMessageRepository,
    val chatParticipantRepository : ChatParticipantRepository,
    val workoutRequestRepository : WorkoutRequestRepository,
    val workoutHistoryRepository: WorkoutHistoryRepository,
    val oAuth2Service : OAuth2Service,
    val time : Time
) : IntegrationTestSupport(){


    @DisplayName("회원A-B간의 최신 핏버디 요청을 조회한다.")
    @Test
    fun findTop1ByFromMemberIdAndToMemberIdOrderByRequestedAtDesc(){
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

        val savedFromMember = memberService.addMember(fromMember)
        val savedToMember = memberService.addMember(toMember)

        val workoutPartnerRequest1 = WorkoutPartnerRequest.of(
            fromMember = savedFromMember,
            toMember = savedToMember,
            now = time.nowLocalDateTime.minusHours(1),
            content = WorkoutPartnerRequestContent.BURN
        )
        val workoutPartnerRequest2 = WorkoutPartnerRequest.of(
            fromMember = savedFromMember,
            toMember = savedToMember,
            now = time.nowLocalDateTime,
            content = WorkoutPartnerRequestContent.BURN
        )
        workoutPartnerRequestRepository.save(workoutPartnerRequest1)
        workoutPartnerRequestRepository.save(workoutPartnerRequest2)

        // when
        val findWorkPartnerRequest = workoutPartnerRequestRepository.findTop1ByFromMemberIdAndToMemberIdOrderByRequestedAtDesc(
            fromMemberId = fromMember.id!!,
            toMemberId = toMember.id!!,
        )

        // then
        assertThat(findWorkPartnerRequest)
            .extracting("fromMember", "toMember", "requestedAt")
            .contains(fromMember, toMember, time.nowLocalDateTime)
    }

    @DisplayName("상대 회원을 향한 특정 운동 파트너 신청을 조회한다.")
    @Test
    fun findByIdAndToMemberId() {
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

        val savedFromMember = memberService.addMember(fromMember)
        val savedToMember = memberService.addMember(toMember)

        val workoutPartnerRequest = WorkoutPartnerRequest.of(
            fromMember = savedFromMember,
            toMember = savedToMember,
            now = time.nowLocalDateTime,
            content = WorkoutPartnerRequestContent.BURN
        )
        workoutPartnerRequestRepository.save(workoutPartnerRequest)

        // when
        val findWorkoutPartnerRequest = workoutPartnerRequestRepository.findByIdAndToMemberId(workoutPartnerRequest.id!!, toMember.id!!)

        // then
        assertThat(findWorkoutPartnerRequest)
            .extracting("fromMember", "toMember", "requestedAt")
            .contains(fromMember, toMember, time.nowLocalDateTime)
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


    @DisplayName("회원이 받은 운동 요청을 조회한다.")
    @Test
    fun findWorkoutPartnerByConditionAndDeletedAtIsNullReceive() {
        // given
        val me = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE,
            providerId = "providerId"
        )
        val otherMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE,
            providerId = "providerId"
        )
        memberRepository.save(me)
        memberRepository.save(otherMember)

        val signupRequest = createOAuth2SignupServiceRequest()
        oAuth2Service.signup(signupRequest, me.id!!)
        oAuth2Service.signup(signupRequest, otherMember.id!!)

        val partnerRequest1 = WorkoutPartnerRequest.of(
            fromMember = otherMember,
            toMember = me,
            now = time.nowLocalDateTime.minusHours(5),
            content = WorkoutPartnerRequestContent.BURN
        )
        val partnerRequest2 = WorkoutPartnerRequest.of(
            fromMember = otherMember,
            toMember = me,
            now = time.nowLocalDateTime.minusHours(3),
            content = WorkoutPartnerRequestContent.BURN
        )
        workoutPartnerRequestRepository.save(partnerRequest1)
        workoutPartnerRequestRepository.save(partnerRequest2)

        val condition = WorkoutPartnerRequestCondition(
            size = 10,
            firstWorkoutPartnerRequestId = null,
            type = WorkoutPartnerRequestType.RECEIVE
        )

        // when
        val slice = workoutPartnerRequestRepository.findWorkoutPartnerByConditionAndDeletedAtIsNull(me.id!!, condition)

        // then
        assertThat(slice.content).hasSize(1)

        val response1 = slice.content[0]

        assertThat(response1.workoutPartnerRequestId)
            .isEqualTo(partnerRequest2.id)

        assertThat(response1.nickname).isEqualTo(otherMember.nickname)
        assertThat(response1.profileImageUrl).isEqualTo(signupRequest.profileImageUrl)
        assertThat(response1.workoutExperience).isEqualTo(otherMember.workoutExperience)
        assertThat(response1.workoutGoal).isEqualTo(otherMember.workoutGoal)
        assertThat(response1.workoutStyle).isEqualTo(otherMember.workoutStyle)

        assertThat(response1.status).isEqualTo(WorkoutPartnerRequestStatusForResponse.PENDING)

        assertThat(response1.chatRoomId).isNull()

        assertThat(slice.hasNext()).isFalse()
    }

    @DisplayName("회원이 보낸 파트너 요청을 조회한다.")
    @Test
    fun findWorkoutPartnerByConditionAndDeletedAtIsNullSend() {
        // given
        val me = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE,
            providerId = "providerId"
        )
        val otherMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE,
            providerId = "providerId"
        )
        memberRepository.save(me)
        memberRepository.save(otherMember)

        val signupRequest = createOAuth2SignupServiceRequest()
        oAuth2Service.signup(signupRequest, me.id!!)
        oAuth2Service.signup(signupRequest, otherMember.id!!)

        val partnerRequest1 = WorkoutPartnerRequest.of(
            fromMember = me,
            toMember = otherMember,
            now = time.nowLocalDateTime.minusHours(5),
            content = WorkoutPartnerRequestContent.BURN
        )
        val partnerRequest2 = WorkoutPartnerRequest.of(
            fromMember = me,
            toMember = otherMember,
            now = time.nowLocalDateTime.minusHours(3),
            content = WorkoutPartnerRequestContent.BURN
        )
        val receivePartnerRequest = WorkoutPartnerRequest.of(
            fromMember = otherMember,
            toMember = me,
            now = time.nowLocalDateTime.minusHours(3),
            content = WorkoutPartnerRequestContent.BURN
        )
        workoutPartnerRequestRepository.save(partnerRequest1)
        workoutPartnerRequestRepository.save(partnerRequest2)
        workoutPartnerRequestRepository.save(receivePartnerRequest)

        val condition = WorkoutPartnerRequestCondition(
            size = 10,
            firstWorkoutPartnerRequestId = null,
            type = WorkoutPartnerRequestType.SEND
        )

        // when
        val slice = workoutPartnerRequestRepository.findWorkoutPartnerByConditionAndDeletedAtIsNull(me.id!!, condition)

        // then
        assertThat(slice.content).hasSize(1)

        val response1 = slice.content[0]

        assertThat(response1.workoutPartnerRequestId)
            .isEqualTo(partnerRequest2.id)

        assertThat(response1.memberId).isEqualTo(otherMember.id!!)
        assertThat(response1.nickname).isEqualTo(otherMember.nickname)
        assertThat(response1.profileImageUrl).isEqualTo(signupRequest.profileImageUrl)
        assertThat(response1.workoutExperience).isEqualTo(otherMember.workoutExperience)
        assertThat(response1.workoutGoal).isEqualTo(otherMember.workoutGoal)
        assertThat(response1.workoutStyle).isEqualTo(otherMember.workoutStyle)

        assertThat(response1.status).isEqualTo(WorkoutPartnerRequestStatusForResponse.PENDING)

        assertThat(response1.chatRoomId).isNull()

        assertThat(slice.hasNext()).isFalse()
    }

    @DisplayName("회원의 파트너 요청을 조회하되, 이미 파트너이고 둘 만의 채팅방이 있으면 채팅방 id를 반환한다.")
    @Test
    fun findWorkoutPartnerByConditionAndDeletedAtExistsChatRoom() {
        // given
        val me = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE,
            providerId = "providerId"
        )
        val otherMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE,
            providerId = "providerId"
        )
        memberRepository.save(me)
        memberRepository.save(otherMember)

        val signupRequest = createOAuth2SignupServiceRequest()
        oAuth2Service.signup(signupRequest, me.id!!)
        oAuth2Service.signup(signupRequest, otherMember.id!!)

        val partnerRequest1 = WorkoutPartnerRequest.of(
            fromMember = otherMember,
            toMember = me,
            now = time.nowLocalDateTime.minusHours(100),
            content = WorkoutPartnerRequestContent.BURN
        )
        val partnerRequest2 = WorkoutPartnerRequest.of(
            fromMember = otherMember,
            toMember = me,
            now = time.nowLocalDateTime.minusHours(3),
            content = WorkoutPartnerRequestContent.BURN
        )
        workoutPartnerRequestRepository.save(partnerRequest1)
        workoutPartnerRequestRepository.save(partnerRequest2)

        val workoutPartner = WorkoutPartner.of(
            memberOne = me,
            memberTwo = otherMember
        )
        workoutPartnerRepository.save(workoutPartner)

        val savedChatRoom = chatRoomRepository.save(ChatRoom(ChatRoomType.PRIVATE))

        val chatParticipant1 = ChatParticipant(
            savedChatRoom,
            me
        )
        val chatParticipant2 = ChatParticipant(
            savedChatRoom,
            otherMember
        )
        chatParticipantRepository.save(chatParticipant1)
        chatParticipantRepository.save(chatParticipant2)

        val condition = WorkoutPartnerRequestCondition(
            size = 10,
            firstWorkoutPartnerRequestId = null,
            type = WorkoutPartnerRequestType.RECEIVE
        )

        // when
        val slice = workoutPartnerRequestRepository.findWorkoutPartnerByConditionAndDeletedAtIsNull(me.id!!, condition)

        // then
        assertThat(slice.content).hasSize(1)

        val response1 = slice.content[0]

        assertThat(response1.workoutPartnerRequestId)
            .isEqualTo(partnerRequest2.id)

        assertThat(response1.nickname).isEqualTo(otherMember.nickname)
        assertThat(response1.profileImageUrl).isEqualTo(signupRequest.profileImageUrl)
        assertThat(response1.workoutExperience).isEqualTo(otherMember.workoutExperience)
        assertThat(response1.workoutGoal).isEqualTo(otherMember.workoutGoal)
        assertThat(response1.workoutStyle).isEqualTo(otherMember.workoutStyle)

        assertThat(response1.status).isEqualTo(WorkoutPartnerRequestStatusForResponse.PENDING)

        assertThat(response1.chatRoomId).isEqualTo(savedChatRoom.id!!)

        assertThat(slice.hasNext()).isFalse()
    }

    @DisplayName("회원의 파트너 요청을 조회하는데 조회했던 마지막 id 부터 조회한다.")
    @Test
    fun findWorkoutPartnerByConditionAndDeletedAtLastId() {
        // given
        val me = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE,
            providerId = "providerId"
        )
        val otherMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE,
            providerId = "providerId"
        )
        val otherMember2 = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE,
            providerId = "providerId"
        )
        memberRepository.save(me)
        memberRepository.save(otherMember)
        memberRepository.save(otherMember2)

        val signupRequest = createOAuth2SignupServiceRequest()
        oAuth2Service.signup(signupRequest, me.id!!)
        oAuth2Service.signup(signupRequest, otherMember.id!!)


        val partnerRequest1 = WorkoutPartnerRequest.of(
            fromMember = otherMember,
            toMember = me,
            now = time.nowLocalDateTime.minusHours(100),
            content = WorkoutPartnerRequestContent.BURN
        )
        val partnerRequest2 = WorkoutPartnerRequest.of(
            fromMember = otherMember,
            toMember = me,
            now = time.nowLocalDateTime.minusHours(10),
            content = WorkoutPartnerRequestContent.BURN
        )
        val partnerRequest3 = WorkoutPartnerRequest.of(
            fromMember = otherMember,
            toMember = me,
            now = time.nowLocalDateTime,
            content = WorkoutPartnerRequestContent.BURN
        )
        val partnerRequest4 = WorkoutPartnerRequest.of(
            fromMember = otherMember2,
            toMember = me,
            now = time.nowLocalDateTime,
            content = WorkoutPartnerRequestContent.BURN
        )
        workoutPartnerRequestRepository.save(partnerRequest1)
        workoutPartnerRequestRepository.save(partnerRequest2)
        workoutPartnerRequestRepository.save(partnerRequest3)
        workoutPartnerRequestRepository.save(partnerRequest4)


        val condition = WorkoutPartnerRequestCondition(
            size = 10,
            firstWorkoutPartnerRequestId = partnerRequest4.id!!,
            type = WorkoutPartnerRequestType.RECEIVE
        )

        // when
        val slice = workoutPartnerRequestRepository.findWorkoutPartnerByConditionAndDeletedAtIsNull(me.id!!, condition)

        // then
        assertThat(slice.content).hasSize(1)

        val response1 = slice.content[0]

        assertThat(response1.workoutPartnerRequestId)
            .isEqualTo(partnerRequest3.id)

        assertThat(response1.nickname).isEqualTo(otherMember.nickname)
        assertThat(response1.profileImageUrl).isEqualTo(signupRequest.profileImageUrl)
        assertThat(response1.workoutExperience).isEqualTo(otherMember.workoutExperience)
        assertThat(response1.workoutGoal).isEqualTo(otherMember.workoutGoal)
        assertThat(response1.workoutStyle).isEqualTo(otherMember.workoutStyle)

        assertThat(response1.status).isEqualTo(WorkoutPartnerRequestStatusForResponse.PENDING)

        assertThat(slice.hasNext()).isFalse()
    }

    @DisplayName("운동 요청 받은 것 조회 시, 상대방 프로필이 조회된다.")
    @Test
    fun findWorkoutPartnerByConditionOtherProfileByReceive() {
        // given
        val me = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE,
            providerId = "providerId"
        )
        val otherMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE,
            providerId = "providerId"
        )
        memberRepository.save(me)
        memberRepository.save(otherMember)

        val signupMeRequest = createOAuth2SignupServiceRequest()
        oAuth2Service.signup(signupMeRequest, me.id!!)

        val signupOtherRequest = createOAuth2SignupServiceRequest(
            nickname = "other",
            workoutExperience = MemberWorkoutExperience.FOUR_TO_SIX_YEARS,
            workoutGoal = MemberWorkoutGoal.BODY_CORRECTION,
            workoutStyle = MemberWorkoutStyle.PARTNER
        )
        oAuth2Service.signup(signupOtherRequest, otherMember.id!!)


        val partnerRequest1 = WorkoutPartnerRequest.of(
            fromMember = otherMember,
            toMember = me,
            now = time.nowLocalDateTime.minusHours(100),
            content = WorkoutPartnerRequestContent.BURN
        )
        workoutPartnerRequestRepository.save(partnerRequest1)


        val condition = WorkoutPartnerRequestCondition(
            size = 10,
            type = WorkoutPartnerRequestType.RECEIVE
        )

        // when
        val slice = workoutPartnerRequestRepository.findWorkoutPartnerByConditionAndDeletedAtIsNull(me.id!!, condition)

        // then
        assertThat(slice.content).hasSize(1)

        val response1 = slice.content[0]

        assertThat(response1.workoutPartnerRequestId)
            .isEqualTo(partnerRequest1.id)

        assertThat(response1.nickname).isEqualTo(otherMember.nickname)
        assertThat(response1.profileImageUrl).isEqualTo(signupOtherRequest.profileImageUrl)
        assertThat(response1.workoutExperience).isEqualTo(otherMember.workoutExperience)
        assertThat(response1.workoutGoal).isEqualTo(otherMember.workoutGoal)
        assertThat(response1.workoutStyle).isEqualTo(otherMember.workoutStyle)

        assertThat(response1.status).isEqualTo(WorkoutPartnerRequestStatusForResponse.PENDING)

        assertThat(slice.hasNext()).isFalse()
    }

    @DisplayName("운동 요청 보낸 것 조회 시, 상대방 프로필이 조회된다.")
    @Test
    fun findWorkoutPartnerByConditionOtherProfileBySend() {
        // given
        val me = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE,
            providerId = "providerId"
        )
        val otherMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE,
            providerId = "providerId"
        )
        memberRepository.save(me)
        memberRepository.save(otherMember)

        val signupMeRequest = createOAuth2SignupServiceRequest()
        oAuth2Service.signup(signupMeRequest, me.id!!)

        val signupOtherRequest = createOAuth2SignupServiceRequest(
            nickname = "other",
            workoutExperience = MemberWorkoutExperience.FOUR_TO_SIX_YEARS,
            workoutGoal = MemberWorkoutGoal.BODY_CORRECTION,
            workoutStyle = MemberWorkoutStyle.PARTNER
        )
        oAuth2Service.signup(signupOtherRequest, otherMember.id!!)


        val partnerRequest1 = WorkoutPartnerRequest.of(
            fromMember = me,
            toMember = otherMember,
            now = time.nowLocalDateTime.minusHours(100),
            content = WorkoutPartnerRequestContent.BURN
        )
        workoutPartnerRequestRepository.save(partnerRequest1)


        val condition = WorkoutPartnerRequestCondition(
            size = 10,
            type = WorkoutPartnerRequestType.SEND
        )

        // when
        val slice = workoutPartnerRequestRepository.findWorkoutPartnerByConditionAndDeletedAtIsNull(me.id!!, condition)

        // then
        assertThat(slice.content).hasSize(1)

        val response1 = slice.content[0]

        assertThat(response1.workoutPartnerRequestId)
            .isEqualTo(partnerRequest1.id)

        assertThat(response1.nickname).isEqualTo(otherMember.nickname)
        assertThat(response1.profileImageUrl).isEqualTo(signupOtherRequest.profileImageUrl)
        assertThat(response1.workoutExperience).isEqualTo(otherMember.workoutExperience)
        assertThat(response1.workoutGoal).isEqualTo(otherMember.workoutGoal)
        assertThat(response1.workoutStyle).isEqualTo(otherMember.workoutStyle)

        assertThat(response1.status).isEqualTo(WorkoutPartnerRequestStatusForResponse.PENDING)

        assertThat(slice.hasNext()).isFalse()
    }

    @DisplayName("운동 요청 보낸 것 조회 시, 제일 처음 운동 사진이 조회된다.")
    @Test
    fun findWorkoutPartnerByConditionWorkoutImageUrl() {
        // given
        val me = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE,
            providerId = "providerId"
        )
        val otherMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE,
            providerId = "providerId"
        )
        memberRepository.save(me)
        memberRepository.save(otherMember)

        val signupMeRequest = createOAuth2SignupServiceRequest()
        oAuth2Service.signup(signupMeRequest, me.id!!)

        val signupOtherRequest = createOAuth2SignupServiceRequest(
            workoutImageUrls = listOf("otherWork1", "otherWork2")
        )
        oAuth2Service.signup(signupOtherRequest, otherMember.id!!)


        val partnerRequest1 = WorkoutPartnerRequest.of(
            fromMember = me,
            toMember = otherMember,
            now = time.nowLocalDateTime.minusHours(100),
            content = WorkoutPartnerRequestContent.BURN
        )
        workoutPartnerRequestRepository.save(partnerRequest1)


        val condition = WorkoutPartnerRequestCondition(
            size = 10,
            type = WorkoutPartnerRequestType.SEND
        )

        // when
        val slice = workoutPartnerRequestRepository.findWorkoutPartnerByConditionAndDeletedAtIsNull(me.id!!, condition)

        // then
        assertThat(slice.content).hasSize(1)

        val response1 = slice.content[0]

        assertThat(response1.workoutPartnerRequestId)
            .isEqualTo(partnerRequest1.id)

        assertThat(response1.workoutImageUrl).isEqualTo("otherWork1")

        assertThat(slice.hasNext()).isFalse()
    }


    @DisplayName("운동 요청 보낸 것 조회 시, 운동 사진이 없으면 운동 사진 조회에 실패한다..")
    @Test
    fun findWorkoutPartnerByConditionNotWorkoutImageUrl() {
        // given
        val me = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE,
            providerId = "providerId"
        )
        val otherMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE,
            providerId = "providerId"
        )
        memberRepository.save(me)
        memberRepository.save(otherMember)

        val signupMeRequest = createOAuth2SignupServiceRequest()
        oAuth2Service.signup(signupMeRequest, me.id!!)

        val signupOtherRequest = createOAuth2SignupServiceRequest(
            workoutImageUrls = listOf()
        )
        oAuth2Service.signup(signupOtherRequest, otherMember.id!!)


        val partnerRequest1 = WorkoutPartnerRequest.of(
            fromMember = me,
            toMember = otherMember,
            now = time.nowLocalDateTime.minusHours(100),
            content = WorkoutPartnerRequestContent.BURN
        )
        workoutPartnerRequestRepository.save(partnerRequest1)


        val condition = WorkoutPartnerRequestCondition(
            size = 10,
            type = WorkoutPartnerRequestType.SEND
        )

        // when
        val slice = workoutPartnerRequestRepository.findWorkoutPartnerByConditionAndDeletedAtIsNull(me.id!!, condition)

        // then
        assertThat(slice.content).hasSize(1)

        val response1 = slice.content[0]

        assertThat(response1.workoutPartnerRequestId)
            .isEqualTo(partnerRequest1.id)

        assertThat(response1.workoutImageUrl).isNull()

        assertThat(slice.hasNext()).isFalse()
    }

    @DisplayName("회원이 받은 운동 요청 조회 시, 각 회원의 최신 요청만 조회한다.")
    @Test
    fun findWorkoutPartnerByConditionNotDuplicatedMember() {
        // given
        val me = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE,
            providerId = "providerId"
        )
        val otherMember1 = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE,
            providerId = "providerId"
        )
        val otherMember2 = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE,
            providerId = "providerId"
        )
        memberRepository.save(me)
        memberRepository.save(otherMember1)
        memberRepository.save(otherMember2)


        val signupRequest = createOAuth2SignupServiceRequest()
        oAuth2Service.signup(signupRequest, me.id!!)
        oAuth2Service.signup(signupRequest, otherMember1.id!!)
        oAuth2Service.signup(signupRequest, otherMember2.id!!)

        val partnerRequest1 = WorkoutPartnerRequest.of(
            fromMember = otherMember1,
            toMember = me,
            now = time.nowLocalDateTime.minusHours(5),
            content = WorkoutPartnerRequestContent.BURN
        )
        val partnerRequest2 = WorkoutPartnerRequest.of(
            fromMember = otherMember1,
            toMember = me,
            now = time.nowLocalDateTime.minusHours(3),
            content = WorkoutPartnerRequestContent.BURN
        )
        val partnerRequest3 = WorkoutPartnerRequest.of(
            fromMember = otherMember2,
            toMember = me,
            now = time.nowLocalDateTime.minusHours(1),
            content = WorkoutPartnerRequestContent.BURN
        )
        workoutPartnerRequestRepository.save(partnerRequest1)
        workoutPartnerRequestRepository.save(partnerRequest2)
        workoutPartnerRequestRepository.save(partnerRequest3)


        val condition = WorkoutPartnerRequestCondition(
            size = 10,
            firstWorkoutPartnerRequestId = null,
            type = WorkoutPartnerRequestType.RECEIVE
        )

        // when
        val slice = workoutPartnerRequestRepository.findWorkoutPartnerByConditionAndDeletedAtIsNull(me.id!!, condition)

        // then
        assertThat(slice.content).hasSize(2)

        val response1 = slice.content[0]
        val response2 = slice.content[1]

        assertThat(response1.workoutPartnerRequestId)
            .isEqualTo(partnerRequest3.id)
        assertThat(response2.workoutPartnerRequestId)
            .isEqualTo(partnerRequest2.id)

        assertThat(slice.hasNext()).isFalse()
    }


    @DisplayName("24시간이 지나 만료된 운동 파트너 요청을 전체 조회한다.")
    @Test
    fun findAllPendingWorkoutPartnerRequestAlreadyExpire() {
        // given
        val me = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE,
            providerId = "providerId"
        )
        val otherMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE,
            providerId = "providerId"
        )
        memberRepository.save(me)
        memberRepository.save(otherMember)

        val signupMeRequest = createOAuth2SignupServiceRequest()
        oAuth2Service.signup(signupMeRequest, me.id!!)

        val signupOtherRequest = createOAuth2SignupServiceRequest(
            workoutImageUrls = listOf()
        )
        oAuth2Service.signup(signupOtherRequest, otherMember.id!!)


        val workoutPartnerRequest1 = WorkoutPartnerRequest.of(
            fromMember = me,
            toMember = otherMember,
            now = time.nowLocalDateTime,
            content = WorkoutPartnerRequestContent.BURN
        )
        workoutPartnerRequestRepository.save(workoutPartnerRequest1)

        val workoutPartnerRequest2 = WorkoutPartnerRequest.of(
            fromMember = me,
            toMember = otherMember,
            now = time.nowLocalDateTime.minusHours(24),
            content = WorkoutPartnerRequestContent.BURN
        )
        workoutPartnerRequestRepository.save(workoutPartnerRequest2)

        // when
        val response = workoutPartnerRequestRepository.findAllPendingWorkoutPartnerRequestAlreadyExpire()

        // then
        assertThat(response).hasSize(1)
        assertThat(response[0].workoutPartnerRequestId).isEqualTo(workoutPartnerRequest2.id!!)
    }

    @DisplayName("특정 대상들의 운동 파트너 요청 전체를 만료 처리한다.")
    @Test
    fun updateExpireByIdIn() {
        // given
        val me = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE,
            providerId = "providerId"
        )
        val otherMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE,
            providerId = "providerId"
        )
        memberRepository.save(me)
        memberRepository.save(otherMember)

        val signupMeRequest = createOAuth2SignupServiceRequest()
        oAuth2Service.signup(signupMeRequest, me.id!!)

        val signupOtherRequest = createOAuth2SignupServiceRequest(
            workoutImageUrls = listOf()
        )
        oAuth2Service.signup(signupOtherRequest, otherMember.id!!)


        val workoutPartnerRequest1 = WorkoutPartnerRequest.of(
            fromMember = me,
            toMember = otherMember,
            now = time.nowLocalDateTime.minusHours(24),
            content = WorkoutPartnerRequestContent.BURN
        )
        workoutPartnerRequestRepository.save(workoutPartnerRequest1)

        val workoutPartnerRequest2 = WorkoutPartnerRequest.of(
            fromMember = me,
            toMember = otherMember,
            now = time.nowLocalDateTime.minusHours(24),
            content = WorkoutPartnerRequestContent.BURN
        )
        workoutPartnerRequestRepository.save(workoutPartnerRequest2)

        val workoutPartnerRequestIds = listOf(workoutPartnerRequest1.id!!, workoutPartnerRequest2.id!!)

        // when
        workoutPartnerRequestRepository.updateExpireByIdIn(workoutPartnerRequestIds)

        // then
        val findWorkoutPartnerRequests = workoutPartnerRequestRepository.findAll()
        assertThat(findWorkoutPartnerRequests).hasSize(2)
        assertThat(findWorkoutPartnerRequests[0].status).isEqualTo(WorkoutPartnerRequestStatus.EXPIRE)
        assertThat(findWorkoutPartnerRequests[1].status).isEqualTo(WorkoutPartnerRequestStatus.EXPIRE)
    }

    @DisplayName("전체 운동 파트너 요청을 조회한다.")
    @Test
    fun findAllWorkoutPartnerRequestBy() {
        // given
        val member1 = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val member2 = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val member3 = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(member1)
        memberRepository.save(member2)
        memberRepository.save(member3)

        val signupRequest = createOAuth2SignupServiceRequest()
        oAuth2Service.signup(signupRequest, member1.id!!)
        oAuth2Service.signup(signupRequest, member2.id!!)
        oAuth2Service.signup(signupRequest, member3.id!!)

        val workoutPartnerRequest1 = WorkoutPartnerRequest.of(
            fromMember = member1,
            toMember = member2,
            now = time.nowLocalDateTime,
            content = WorkoutPartnerRequestContent.BURN
        )
        workoutPartnerRequestRepository.save(workoutPartnerRequest1)

        val workoutPartnerRequest2 = WorkoutPartnerRequest.of(
            fromMember = member2,
            toMember = member1,
            now = time.nowLocalDateTime,
            content = WorkoutPartnerRequestContent.BURN
        )
        workoutPartnerRequestRepository.save(workoutPartnerRequest2)

        val workoutPartnerRequest3 = WorkoutPartnerRequest.of(
            fromMember = member1,
            toMember = member3,
            now = time.nowLocalDateTime,
            content = WorkoutPartnerRequestContent.BURN
        )
        workoutPartnerRequestRepository.save(workoutPartnerRequest3)

        val workoutPartner = WorkoutPartner(
            memberOne = member1,
            memberTwo = member2,
        )
        workoutPartnerRepository.save(workoutPartner)

        val chatRoom = ChatRoom(ChatRoomType.PRIVATE)
        chatRoomRepository.save(chatRoom)

        val chatParticipant1 = ChatParticipant(
            chatRoom = chatRoom,
            member = member1,
        )
        chatParticipantRepository.save(chatParticipant1)

        val chatParticipant2 = ChatParticipant(
            chatRoom = chatRoom,
            member = member2,
        )
        chatParticipantRepository.save(chatParticipant2)

        val chatMessage = ChatMessage.ofWorkoutRequest(
            member = member1,
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage)

        val workoutRequest = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = member1,
            toMember = member2,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        workoutRequestRepository.save(workoutRequest)

        val workoutHistory = WorkoutHistory(
            chatRoom = chatRoom,
            workoutRequest = workoutRequest,
            memberOne = member1,
            memberTwo = member2,
            completedAt = time.nowLocalDateTime
        )
        workoutHistoryRepository.save(workoutHistory)

        val condition = AdminWorkoutPartnerRequestCondition()

        // when
        val response = workoutPartnerRequestRepository.findAllWorkoutPartnerRequestBy(condition)

        // then
        assertThat(response)
            .extracting(
                "workoutPartnerRequestId",
                "workoutPartnerRequestStatus",
                "respondedAt",
                "hasChatRoom",
                "workoutHistoryCount"
            )
            .contains(
                tuple(
                    workoutPartnerRequest1.id!!,
                    workoutPartnerRequest1.status,
                    workoutPartnerRequest1.respondedAt,
                    true,
                    1L
                ),
                tuple(
                    workoutPartnerRequest2.id!!,
                    workoutPartnerRequest2.status,
                    workoutPartnerRequest2.respondedAt,
                    true,
                    1L
                ),
                tuple(
                    workoutPartnerRequest3.id!!,
                    workoutPartnerRequest3.status,
                    workoutPartnerRequest3.respondedAt,
                    false,
                    0L
                ),
            )
    }

    @DisplayName("전체 운동 파트너 요청을 조회 시, 운동 파트너 요청 id가 있으면 그것보다 더 낮은 id의 운동 파트너 요청을 조회한다..")
    @Test
    fun findAllWorkoutPartnerRequestByExistsConditionWorkoutPartnerRequestId() {
        // given
        val member1 = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val member2 = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val member3 = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(member1)
        memberRepository.save(member2)
        memberRepository.save(member3)

        val signupRequest = createOAuth2SignupServiceRequest()
        oAuth2Service.signup(signupRequest, member1.id!!)
        oAuth2Service.signup(signupRequest, member2.id!!)
        oAuth2Service.signup(signupRequest, member3.id!!)

        val workoutPartnerRequest1 = WorkoutPartnerRequest.of(
            fromMember = member1,
            toMember = member2,
            now = time.nowLocalDateTime,
            content = WorkoutPartnerRequestContent.BURN
        )
        workoutPartnerRequestRepository.save(workoutPartnerRequest1)

        val workoutPartnerRequest2 = WorkoutPartnerRequest.of(
            fromMember = member2,
            toMember = member1,
            now = time.nowLocalDateTime,
            content = WorkoutPartnerRequestContent.BURN
        )
        workoutPartnerRequestRepository.save(workoutPartnerRequest2)

        val workoutPartnerRequest3 = WorkoutPartnerRequest.of(
            fromMember = member1,
            toMember = member3,
            now = time.nowLocalDateTime,
            content = WorkoutPartnerRequestContent.BURN
        )
        workoutPartnerRequestRepository.save(workoutPartnerRequest3)

        val condition = AdminWorkoutPartnerRequestCondition(
            workoutPartnerRequestId = workoutPartnerRequest3.id!!
        )

        // when
        val response = workoutPartnerRequestRepository.findAllWorkoutPartnerRequestBy(condition)

        // then
        assertThat(response).hasSize(2)
        assertThat(response)
            .extracting("workoutPartnerRequestId")
            .containsExactly(
                workoutPartnerRequest2.id!!,
                workoutPartnerRequest1.id!!
            )
        assertThat(response.hasNext()).isEqualTo(false)
    }

    @DisplayName("본인, 상대방 사이의 제일 최근 파트너 요청을 조회한다.")
    @Test
    fun findAllLatestWorkoutPartnerRequest() {
        // given
        val meMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val otherMember1 = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val otherMember2 = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(meMember)
        memberRepository.save(otherMember1)
        memberRepository.save(otherMember2)

        val signupRequest = createOAuth2SignupServiceRequest()
        oAuth2Service.signup(signupRequest, meMember.id!!)
        oAuth2Service.signup(signupRequest, otherMember1.id!!)
        oAuth2Service.signup(signupRequest, otherMember2.id!!)

        val workoutPartnerRequest1 = WorkoutPartnerRequest.of(
            fromMember = meMember,
            toMember = otherMember1,
            now = time.nowLocalDateTime,
            content = WorkoutPartnerRequestContent.BURN
        )
        workoutPartnerRequestRepository.save(workoutPartnerRequest1)

        val workoutPartnerRequest2 = WorkoutPartnerRequest.of(
            fromMember = otherMember2,
            toMember = meMember,
            now = time.nowLocalDateTime,
            content = WorkoutPartnerRequestContent.BURN
        )
        workoutPartnerRequestRepository.save(workoutPartnerRequest2)

        val otherMemberIds = listOf(otherMember1.id!!, otherMember2.id!!)

        // when
        val response = workoutPartnerRequestRepository.findAllLatestWorkoutPartnerRequest(
            meMemberId = meMember.id!!,
            otherMemberIds = otherMemberIds
        )

        // then
        assertThat(response).hasSize(2)
        assertThat(response)
            .extracting("id", "fromMember", "toMember")
            .contains(
                tuple(workoutPartnerRequest1.id!!, meMember, otherMember1),
                tuple(workoutPartnerRequest2.id!!, otherMember2, meMember),
            )
    }

    @DisplayName("제일 최근 파트너 요청 조회 시, 서로 간의 운동 파트너 요청이 여러개라면 제일 최근 것만 조회한다.")
    @Test
    fun findAllLatestWorkoutPartnerRequestDuplicated() {
        // given
        val meMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val otherMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(meMember)
        memberRepository.save(otherMember)

        val signupRequest = createOAuth2SignupServiceRequest()
        oAuth2Service.signup(signupRequest, meMember.id!!)
        oAuth2Service.signup(signupRequest, otherMember.id!!)

        val workoutPartnerRequest1 = WorkoutPartnerRequest.of(
            fromMember = meMember,
            toMember = otherMember,
            now = time.nowLocalDateTime.minusSeconds(1),
            content = WorkoutPartnerRequestContent.BURN
        )
        workoutPartnerRequestRepository.save(workoutPartnerRequest1)

        val workoutPartnerRequest2 = WorkoutPartnerRequest.of(
            fromMember = otherMember,
            toMember = meMember,
            now = time.nowLocalDateTime,
            content = WorkoutPartnerRequestContent.BURN
        )
        workoutPartnerRequestRepository.save(workoutPartnerRequest2)

        val otherMemberIds = listOf(otherMember.id!!)

        // when
        val response = workoutPartnerRequestRepository.findAllLatestWorkoutPartnerRequest(
            meMemberId = meMember.id!!,
            otherMemberIds = otherMemberIds
        )

        // then
        assertThat(response).hasSize(1)
        assertThat(response)
            .extracting("id", "fromMember", "toMember")
            .contains(
                tuple(workoutPartnerRequest2.id!!, otherMember, meMember),
            )
    }

    @DisplayName("제일 최근 파트너 요청 조회 시, 서로 간의 운동 파트너 요청 시간이 겹치면, 상대방이 보낸 운동 파트너 요청을 조회한다.")
    @Test
    fun findAllLatestWorkoutPartnerRequestDuplicated2() {
        // given
        val meMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val otherMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(meMember)
        memberRepository.save(otherMember)

        val signupRequest = createOAuth2SignupServiceRequest()
        oAuth2Service.signup(signupRequest, meMember.id!!)
        oAuth2Service.signup(signupRequest, otherMember.id!!)

        val workoutPartnerRequest1 = WorkoutPartnerRequest.of(
            fromMember = meMember,
            toMember = otherMember,
            now = time.nowLocalDateTime,
            content = WorkoutPartnerRequestContent.BURN
        )
        workoutPartnerRequestRepository.save(workoutPartnerRequest1)

        val workoutPartnerRequest2 = WorkoutPartnerRequest.of(
            fromMember = otherMember,
            toMember = meMember,
            now = time.nowLocalDateTime,
            content = WorkoutPartnerRequestContent.BURN
        )
        workoutPartnerRequestRepository.save(workoutPartnerRequest2)

        val otherMemberIds = listOf(otherMember.id!!)

        // when
        val response = workoutPartnerRequestRepository.findAllLatestWorkoutPartnerRequest(
            meMemberId = meMember.id!!,
            otherMemberIds = otherMemberIds
        )

        // then
        assertThat(response).hasSize(1)
        assertThat(response)
            .extracting("id", "fromMember", "toMember")
            .contains(
                tuple(workoutPartnerRequest2.id!!, otherMember, meMember),
            )
    }
}