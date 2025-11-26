package kr.co.fitview.api.app.domain.workout_partner.repository

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.address.dto.request.AddressCreateServiceRequest
import kr.co.fitview.api.app.domain.address.entity.enums.AddressSiDo
import kr.co.fitview.api.app.domain.chat.entity.ChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
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
import kr.co.fitview.api.app.domain.workout_partner.condition.WorkoutPartnerRequestCondition
import kr.co.fitview.api.app.domain.workout_partner.dto.request.enums.WorkoutPartnerRequestType
import kr.co.fitview.api.app.domain.workout_partner.dto.response.enums.WorkoutPartnerRequestStatusForResponse
import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartner
import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartnerRequest
import kr.co.fitview.api.app.domain.workout_partner.entity.enums.WorkoutPartnerRequestContent
import kr.co.fitview.api.app.global.entity.Gender
import kr.co.fitview.api.app.global.entity.OAuth2Provider
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
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
    val chatParticipantRepository : ChatParticipantRepository,
    val oAuth2Service : OAuth2Service,
    val time : Time
) : IntegrationTestSupport(){


    @DisplayName("회원A-B간의 최신 핏버디 요청을 조회한다.")
    @Test
    fun findTop1ByFromMemberIdAndToMemberIdAndDeletedAtIsNullOrderByRequestedAtDesc(){
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
        val findWorkPartnerRequest = workoutPartnerRequestRepository.findTop1ByFromMemberIdAndToMemberIdAndDeletedAtIsNullOrderByRequestedAtDesc(
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
    fun findByIdAndToMemberIdAndDeletedAtIsNull() {
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
        val findWorkoutPartnerRequest = workoutPartnerRequestRepository.findByIdAndToMemberIdAndDeletedAtIsNull(workoutPartnerRequest.id!!, toMember.id!!)

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
        assertThat(slice.content).hasSize(2)

        val response1 = slice.content[0]
        val response2 = slice.content[1]

        assertThat(response1.workoutPartnerRequestId)
            .isEqualTo(partnerRequest2.id)
        assertThat(response2.workoutPartnerRequestId)
            .isEqualTo(partnerRequest1.id)

        assertThat(response1.nickname).isEqualTo(otherMember.nickname)
        assertThat(response1.profileImageUrl).isEqualTo(signupRequest.profileImageUrl)
        assertThat(response1.workoutExperience).isEqualTo(otherMember.workoutExperience)
        assertThat(response1.workoutGoal).isEqualTo(otherMember.workoutGoal)
        assertThat(response1.workoutStyle).isEqualTo(otherMember.workoutStyle)

        assertThat(response1.status).isEqualTo(WorkoutPartnerRequestStatusForResponse.PENDING)
        assertThat(response2.status).isEqualTo(WorkoutPartnerRequestStatusForResponse.PENDING)

        assertThat(response1.chatRoomId).isNull()
        assertThat(response2.chatRoomId).isNull()

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
        assertThat(slice.content).hasSize(2)

        val response1 = slice.content[0]
        val response2 = slice.content[1]

        assertThat(response1.workoutPartnerRequestId)
            .isEqualTo(partnerRequest2.id)
        assertThat(response2.workoutPartnerRequestId)
            .isEqualTo(partnerRequest1.id)

        assertThat(response1.nickname).isEqualTo(otherMember.nickname)
        assertThat(response1.profileImageUrl).isEqualTo(signupRequest.profileImageUrl)
        assertThat(response1.workoutExperience).isEqualTo(otherMember.workoutExperience)
        assertThat(response1.workoutGoal).isEqualTo(otherMember.workoutGoal)
        assertThat(response1.workoutStyle).isEqualTo(otherMember.workoutStyle)

        assertThat(response1.status).isEqualTo(WorkoutPartnerRequestStatusForResponse.PENDING)
        assertThat(response2.status).isEqualTo(WorkoutPartnerRequestStatusForResponse.PENDING)

        assertThat(response1.chatRoomId).isNull()
        assertThat(response2.chatRoomId).isNull()

        assertThat(slice.hasNext()).isFalse()
    }

    @DisplayName("회원의 파트너 요청을 조회하되, 이미 파트너이고 둘 만의 채팅방이 있으면 반환한다.")
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
        assertThat(slice.content).hasSize(2)

        val response1 = slice.content[0]
        val response2 = slice.content[1]

        assertThat(response1.workoutPartnerRequestId)
            .isEqualTo(partnerRequest2.id)
        assertThat(response2.workoutPartnerRequestId)
            .isEqualTo(partnerRequest1.id)

        assertThat(response1.nickname).isEqualTo(otherMember.nickname)
        assertThat(response1.profileImageUrl).isEqualTo(signupRequest.profileImageUrl)
        assertThat(response1.workoutExperience).isEqualTo(otherMember.workoutExperience)
        assertThat(response1.workoutGoal).isEqualTo(otherMember.workoutGoal)
        assertThat(response1.workoutStyle).isEqualTo(otherMember.workoutStyle)

        assertThat(response1.status).isEqualTo(WorkoutPartnerRequestStatusForResponse.PENDING)
        assertThat(response2.status).isEqualTo(WorkoutPartnerRequestStatusForResponse.PENDING)

        assertThat(response1.chatRoomId).isEqualTo(savedChatRoom.id!!)
        assertThat(response2.chatRoomId).isEqualTo(savedChatRoom.id!!)

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
            now = time.nowLocalDateTime.minusHours(10),
            content = WorkoutPartnerRequestContent.BURN
        )
        val partnerRequest3 = WorkoutPartnerRequest.of(
            fromMember = otherMember,
            toMember = me,
            now = time.nowLocalDateTime,
            content = WorkoutPartnerRequestContent.BURN
        )
        workoutPartnerRequestRepository.save(partnerRequest1)
        workoutPartnerRequestRepository.save(partnerRequest2)
        workoutPartnerRequestRepository.save(partnerRequest3)


        val condition = WorkoutPartnerRequestCondition(
            size = 10,
            firstWorkoutPartnerRequestId = partnerRequest3.id!!,
            type = WorkoutPartnerRequestType.RECEIVE
        )

        // when
        val slice = workoutPartnerRequestRepository.findWorkoutPartnerByConditionAndDeletedAtIsNull(me.id!!, condition)

        // then
        assertThat(slice.content).hasSize(2)

        val response1 = slice.content[0]
        val response2 = slice.content[1]

        assertThat(response1.workoutPartnerRequestId)
            .isEqualTo(partnerRequest2.id)
        assertThat(response2.workoutPartnerRequestId)
            .isEqualTo(partnerRequest1.id)

        assertThat(response1.nickname).isEqualTo(otherMember.nickname)
        assertThat(response1.profileImageUrl).isEqualTo(signupRequest.profileImageUrl)
        assertThat(response1.workoutExperience).isEqualTo(otherMember.workoutExperience)
        assertThat(response1.workoutGoal).isEqualTo(otherMember.workoutGoal)
        assertThat(response1.workoutStyle).isEqualTo(otherMember.workoutStyle)

        assertThat(response1.status).isEqualTo(WorkoutPartnerRequestStatusForResponse.PENDING)
        assertThat(response2.status).isEqualTo(WorkoutPartnerRequestStatusForResponse.PENDING)

        assertThat(slice.hasNext()).isFalse()
    }
}