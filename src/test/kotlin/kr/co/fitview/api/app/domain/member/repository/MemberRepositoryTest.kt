package kr.co.fitview.api.app.domain.member.repository

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.address.constant.AddressConstant
import kr.co.fitview.api.app.domain.address.dto.request.AddressCreateServiceRequest
import kr.co.fitview.api.app.domain.address.entity.Address
import kr.co.fitview.api.app.domain.address.entity.enums.AddressSiDo
import kr.co.fitview.api.app.domain.address.repository.AddressRepository
import kr.co.fitview.api.app.domain.chat.entity.ChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.chat.repository.ChatParticipantRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatRoomRepository
import kr.co.fitview.api.app.domain.member.condition.MemberLocalCondition
import kr.co.fitview.api.app.domain.member.dto.BoundingBox
import kr.co.fitview.api.app.domain.member.dto.request.Age
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle
import kr.co.fitview.api.app.domain.member.entity.enums.WorkoutTimeName
import kr.co.fitview.api.app.domain.oauth2.dto.request.OAuth2SignupServiceRequest
import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.global.entity.Gender
import kr.co.fitview.api.app.global.entity.OAuth2Provider
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import kr.co.fitview.api.app.global.util.TestDataFactory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate
import kotlin.math.cos


class MemberRepositoryTest@Autowired constructor(
    val memberRepository : MemberRepository,
    val addressRepository: AddressRepository,
    val oAuth2Service : OAuth2Service,
    val chatRoomRepository : ChatRoomRepository,
    val chatParticipantRepository : ChatParticipantRepository,
    val time : Time
) : IntegrationTestSupport() {


    @DisplayName("로그인 id로 해당 회원을 조회한다.")
    @Test
    fun findByEmailAndDeletedAtIsNull() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val savedMember = memberRepository.save(member)

        // when
        val findMember = memberRepository.findByEmailAndDeletedAtIsNull(savedMember.email!!)

        // then
        assertThat(savedMember.id).isNotNull()
        assertThat(findMember)
            .extracting("email", "password", "role")
            .contains(savedMember.email, savedMember.password, savedMember.role)
    }

    @DisplayName("저장되지 않은 회원은 로그인 id로 해당 회원을 조회할 수 없다.")
    @Test
    fun findByEmailAndDeletedAtIsNullWithoutMember() {
        // given
        val loginId = "loginId"

        // when
        val findMember = memberRepository.findByEmailAndDeletedAtIsNull(loginId)

        // then
        assertThat(findMember).isNull()
    }


    @DisplayName("회원 고유 id로 해당 회원을 조회한다.")
    @Test
    fun findByIdAndDeletedAtIsNull() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val savedMember = memberRepository.save(member)

        // when
        val findMember = memberRepository.findByIdAndDeletedAtIsNull(savedMember.id!!)

        // then
        assertThat(savedMember.id).isNotNull()
        assertThat(findMember)
            .extracting("email", "password", "role")
            .contains(savedMember.email, savedMember.password, savedMember.role)
    }


    @DisplayName("저장되지 않은 회원은 회원 고유 id로 해당 회원을 조회할 수 없다.")
    @Test
    fun findByIdAndDeletedAtIsNullWithoutMember() {
        // given
        val memberId = 100L

        // when
        val findMember = memberRepository.findByIdAndDeletedAtIsNull(memberId)

        // then
        assertThat(findMember).isNull()
    }


    @DisplayName("소셜 로그인 고유 id로 해당 회원을 조회한다.")
    @Test
    fun findByProviderIdAndDeletedAtIsNull() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE,
            providerId = "providerId"
        )
        val savedMember = memberRepository.save(member)

        // when
        val findMember = memberRepository.findByProviderIdAndDeletedAtIsNull(savedMember.providerId!!)

        // then
        assertThat(savedMember.id).isNotNull()
        assertThat(findMember)
            .extracting("email", "password", "role", "provider", "providerId")
            .contains(savedMember.email, savedMember.password, savedMember.role, savedMember.provider, savedMember.providerId)

    }


    @DisplayName("저장되지 않은 회원은 소셜 로그인 고유 id로 해당 회원을 조회할 수 없다.")
    @Test
    fun findByProviderIdAndDeletedAtIsNullWithoutMember() {
        // given
        val providerId = "providerId"

        // when
        val findMember = memberRepository.findByProviderIdAndDeletedAtIsNull(providerId)

        // then
        assertThat(findMember).isNull()
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


    @DisplayName("회원 프로필을 조회한다.")
    @Test
    fun findMemberProfileByDeletedAtIsNull() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE,
            providerId = "providerId"
        )
        val savedMember = memberRepository.save(member)

        val request = createOAuth2SignupServiceRequest()
        oAuth2Service.signup(request, member.id!!)

        // when
        val response = memberRepository.findMemberProfileByDeletedAtIsNull(member.id!!)

        // then
        assertThat(response).isNotNull
        response?.let {
            assertThat(it.memberId).isEqualTo(savedMember.id)
            assertThat(it.nickname).isEqualTo(request.nickname)
            assertThat(it.gender).isEqualTo(request.gender)
            assertThat(it.height).isEqualTo(request.height)
            assertThat(it.weight).isEqualTo(request.weight)
            assertThat(it.intro).isEqualTo(request.intro)
            assertThat(it.age).isEqualTo(Age.fromBirthDay(request.birthday))
            assertThat(it.workoutExperience).isEqualTo(request.workoutExperience)
            assertThat(it.workoutStyle).isEqualTo(request.workoutStyle)
            assertThat(it.workoutGoal).isEqualTo(request.workoutGoal)
            assertThat(it.score).isEqualTo(savedMember.score!!.toInt())

            assertThat(it.siDo).isEqualTo(request.address.siDo)
            assertThat(it.siGunGu).isEqualTo(request.address.siGunGu)
            assertThat(it.eupMyeonDong).isEqualTo(request.address.eupMyeonDong)

            assertThat(it.workoutTimeNames).containsExactlyInAnyOrderElementsOf(request.workoutTimes)
            assertThat(it.workoutImageUrls).containsExactlyInAnyOrderElementsOf(request.workoutImageUrls)

            assertThat(it.profileImageUrl).isEqualTo(request.profileImageUrl)
        }
    }

    @DisplayName("회원의 채팅방 프로필을 확인한다.")
    @Test
    fun findMemberChatProfileByDeletedAtIsNull() {
        // given
        val me = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(me)

        val request = createOAuth2SignupServiceRequest()
        oAuth2Service.signup(request, me.id!!)

        // when
        val response = memberRepository.findMemberChatProfileByDeletedAtIsNull(me.id!!)

        // then
        assertThat(response)
            .extracting("profileImageUrl", "nickname")
            .contains(request.profileImageUrl, request.nickname)
    }

    @DisplayName("운동 파트너 요청에 쓰이는 회원 프로필을 조회한다.")
    @Test
    fun findMemberWorkoutRequestProfile() {
        // given
        val me = Member(
            email = "email1",
            password = "password",
            role = Role.USER
        )
        val member1 = Member(
            email = "email2",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(member1)
        memberRepository.save(me)


        val request = createOAuth2SignupServiceRequest()
        oAuth2Service.signup(request, me.id!!)

        // when
        val findMemberProfile = memberRepository.findMemberWorkoutRequestProfile(me.id!!)

        // then
        assertThat(findMemberProfile)
            .extracting("memberId", "profileImageUrl", "nickname")
            .contains(me.id!!, request.profileImageUrl, request.nickname)
    }


    private fun createBoundingBox(address: Address) : BoundingBox {
        var lat = address.lat
        var lng = address.lng

        if(address.siDo != AddressSiDo.SEOUL){
            lat = AddressConstant.DEFAULT_LAT
            lng = AddressConstant.DEFAULT_LNG
        }

        val latDeg = address.radiusKm?.div(111)
        val latRad = Math.toRadians(address.lat!!)
        val lngDeg = address.radiusKm?.div(111 * cos(latRad))

        val minLat = lat?.minus(latDeg!!)
        val maxLat = lat?.plus(latDeg!!)

        val minLng = lng?.minus(lngDeg!!)
        val maxLng = lng?.plus(lngDeg!!)

        return BoundingBox(
            minLat = minLat!!,
            maxLat = maxLat!!,
            minLng = minLng!!,
            maxLng = maxLng!!
        )
    }

    @DisplayName("현재 회원 인근에 존재하는 회원을 조회한다.")
    @Test
    fun findMemberWithinLocal() {
        // given
        val baseMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(baseMember)
        val address = Address(
            member = baseMember,
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 50.0,
            lng = 50.0,
            fullAddress = "fullAddress",
            radiusKm = 10.0
        )
        addressRepository.save(address)

        val boundingBox = createBoundingBox(address)

        // BoundingBox(minLat=49.909909909909906, maxLat=50.090090090090094, minLng=49.85984470028284, maxLng=50.14015529971716)

        val member1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member1)
        val inAddress1 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 49.91,
            lng = 50.14,
            fullAddress = "fullAddress"
        )
        val signupRequest1 = TestDataFactory.oAuth2SignupRequest(
            nickname = "nickname1",
            workoutExperience = MemberWorkoutExperience.JUST_STARTED,
            workoutStyle = MemberWorkoutStyle.STRENGTH,
            workoutGoal = MemberWorkoutGoal.HEALTH_MAINTENANCE,
            profileImageUrl = "profileImageUrl1",
            address = inAddress1
        )
        oAuth2Service.signup(signupRequest1, member1.id!!)


        val member2 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member2)
        val inAddress2 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 50.00,
            lng = 50.00,
            fullAddress = "fullAddress"
        )
        val signupRequest2 = TestDataFactory.oAuth2SignupRequest(
            nickname = "nickname2",
            workoutExperience = MemberWorkoutExperience.FOUR_TO_SIX_YEARS,
            workoutStyle = MemberWorkoutStyle.PERFORMANCE,
            workoutGoal = MemberWorkoutGoal.STRENGTH_GAIN,
            profileImageUrl = "profileImageUrl2",
            address = inAddress2
        )
        oAuth2Service.signup(signupRequest2, member2.id!!)


        val member3 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member3)
        val inAddress3 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 49.91,
            lng = 49.86,
            fullAddress = "fullAddress"
        )
        val signupRequest3 = TestDataFactory.oAuth2SignupRequest(
            nickname = "nickname3",
            workoutExperience = MemberWorkoutExperience.ONE_TO_THREE_YEARS,
            workoutStyle = MemberWorkoutStyle.PARTNER,
            workoutGoal = MemberWorkoutGoal.STRESS_RELIEF,
            profileImageUrl = "profileImageUrl3",
            address = inAddress3
        )
        oAuth2Service.signup(signupRequest3, member3.id!!)


        val member4 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member4)
        val outAddress1 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 49.90,
            lng = 49.85,
            fullAddress = "fullAddress"
        )
        val signupRequest4 = TestDataFactory.oAuth2SignupRequest(
            address = outAddress1
        )
        oAuth2Service.signup(signupRequest4, member4.id!!)

        val member5 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member5)
        val outAddress2 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 49.90,
            lng = 49.85,
            fullAddress = "fullAddress"
        )
        val signupRequest5 = TestDataFactory.oAuth2SignupRequest(
            address = outAddress2
        )
        oAuth2Service.signup(signupRequest5, member5.id!!)


        val condition = MemberLocalCondition(
            size = 10,
            page = 1
        )

        // when
        val response = memberRepository.findMemberWithinLocal(baseMember.id!!, member5.id!!, boundingBox, condition)

        // then
        assertThat(response)
            .extracting("memberId", "nickname", "workoutExperience", "workoutStyle", "workoutGoal", "profileImageUrl")
            .containsExactlyInAnyOrder(
                tuple(
                    member3.id!!,
                    signupRequest3.nickname,
                    signupRequest3.workoutExperience,
                    signupRequest3.workoutStyle,
                    signupRequest3.workoutGoal,
                    signupRequest3.profileImageUrl,
                ),
                tuple(
                    member2.id!!,
                    signupRequest2.nickname,
                    signupRequest2.workoutExperience,
                    signupRequest2.workoutStyle,
                    signupRequest2.workoutGoal,
                    signupRequest2.profileImageUrl,
                ),
                tuple(
                    member1.id!!,
                    signupRequest1.nickname,
                    signupRequest1.workoutExperience,
                    signupRequest1.workoutStyle,
                    signupRequest1.workoutGoal,
                    signupRequest1.profileImageUrl,
                ),
            )
    }


    @DisplayName("인근 회원 조회 시, randomId를 기점으로 오른쪽 회원 조회 + 부족하면 맨 처음 회원 조회순으로 가져온다..")
    @Test
    fun findMemberWithinLocalLeftAndRight() {
        // given
        val baseMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(baseMember)
        val address = Address(
            member = baseMember,
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 50.0,
            lng = 50.0,
            fullAddress = "fullAddress",
            radiusKm = 10.0
        )
        addressRepository.save(address)
        val boundingBox = createBoundingBox(address)

        // BoundingBox(minLat=49.909909909909906, maxLat=50.090090090090094, minLng=49.85984470028284, maxLng=50.14015529971716)

        val member1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member1)
        val inAddress1 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 50.0,
            lng = 50.0,
            fullAddress = "fullAddress"
        )
        val signupRequest1 = TestDataFactory.oAuth2SignupRequest(
            nickname = "nickname1",
            workoutExperience = MemberWorkoutExperience.JUST_STARTED,
            workoutStyle = MemberWorkoutStyle.STRENGTH,
            workoutGoal = MemberWorkoutGoal.HEALTH_MAINTENANCE,
            profileImageUrl = "profileImageUrl1",
            address = inAddress1
        )
        oAuth2Service.signup(signupRequest1, member1.id!!)


        val member2 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member2)
        val inAddress2 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 50.00,
            lng = 50.00,
            fullAddress = "fullAddress"
        )
        val signupRequest2 = TestDataFactory.oAuth2SignupRequest(
            nickname = "nickname2",
            workoutExperience = MemberWorkoutExperience.FOUR_TO_SIX_YEARS,
            workoutStyle = MemberWorkoutStyle.PERFORMANCE,
            workoutGoal = MemberWorkoutGoal.STRENGTH_GAIN,
            profileImageUrl = "profileImageUrl2",
            address = inAddress2
        )
        oAuth2Service.signup(signupRequest2, member2.id!!)


        val member3 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member3)
        val inAddress3 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 50.0,
            lng = 50.0,
            fullAddress = "fullAddress"
        )
        val signupRequest3 = TestDataFactory.oAuth2SignupRequest(
            nickname = "nickname3",
            workoutExperience = MemberWorkoutExperience.ONE_TO_THREE_YEARS,
            workoutStyle = MemberWorkoutStyle.PARTNER,
            workoutGoal = MemberWorkoutGoal.STRESS_RELIEF,
            profileImageUrl = "profileImageUrl3",
            address = inAddress3
        )
        oAuth2Service.signup(signupRequest3, member3.id!!)


        val member4 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member4)
        val outAddress1 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 49.90,
            lng = 49.85,
            fullAddress = "fullAddress"
        )
        val signupRequest4 = TestDataFactory.oAuth2SignupRequest(
            address = outAddress1
        )
        oAuth2Service.signup(signupRequest4, member4.id!!)

        val condition = MemberLocalCondition(
            size = 10,
            page = 1
        )

        // when
        val response = memberRepository.findMemberWithinLocal(baseMember.id!!, member2.id!!, boundingBox, condition)

        // then
        assertThat(response).hasSize(3)
        assertThat(response[0].memberId).isEqualTo(member2.id!!)
        assertThat(response[1].memberId).isEqualTo(member3.id!!)
        assertThat(response[2].memberId).isEqualTo(member1.id!!)
    }


    @DisplayName("인근 회원 조회 시, 최소 운동 경험이 있는 사람을 조회한다..")
    @Test
    fun findMemberWithinLocalMinWorkoutExperience() {
        // given
        val baseMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(baseMember)
        val address = Address(
            member = baseMember,
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 50.0,
            lng = 50.0,
            fullAddress = "fullAddress",
            radiusKm = 10.0
        )
        addressRepository.save(address)
        val boundingBox = createBoundingBox(address)

        // BoundingBox(minLat=49.909909909909906, maxLat=50.090090090090094, minLng=49.85984470028284, maxLng=50.14015529971716)

        val member1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member1)
        val inAddress1 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 50.0,
            lng = 50.0,
            fullAddress = "fullAddress"
        )
        val signupRequest1 = TestDataFactory.oAuth2SignupRequest(
            nickname = "nickname1",
            workoutExperience = MemberWorkoutExperience.UNDER_ONE_YEAR,
            workoutStyle = MemberWorkoutStyle.STRENGTH,
            workoutGoal = MemberWorkoutGoal.HEALTH_MAINTENANCE,
            profileImageUrl = "profileImageUrl1",
            address = inAddress1
        )
        oAuth2Service.signup(signupRequest1, member1.id!!)


        val member2 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member2)
        val inAddress2 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 50.00,
            lng = 50.00,
            fullAddress = "fullAddress"
        )
        val signupRequest2 = TestDataFactory.oAuth2SignupRequest(
            nickname = "nickname2",
            workoutExperience = MemberWorkoutExperience.FOUR_TO_SIX_YEARS,
            workoutStyle = MemberWorkoutStyle.PERFORMANCE,
            workoutGoal = MemberWorkoutGoal.STRENGTH_GAIN,
            profileImageUrl = "profileImageUrl2",
            address = inAddress2
        )
        oAuth2Service.signup(signupRequest2, member2.id!!)


        val member3 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member3)
        val inAddress3 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 50.0,
            lng = 50.0,
            fullAddress = "fullAddress"
        )
        val signupRequest3 = TestDataFactory.oAuth2SignupRequest(
            nickname = "nickname3",
            workoutExperience = MemberWorkoutExperience.ONE_TO_THREE_YEARS,
            workoutStyle = MemberWorkoutStyle.PARTNER,
            workoutGoal = MemberWorkoutGoal.STRESS_RELIEF,
            profileImageUrl = "profileImageUrl3",
            address = inAddress3
        )
        oAuth2Service.signup(signupRequest3, member3.id!!)


        val condition = MemberLocalCondition(
            size = 10,
            page = 1,
            minWorkoutExperience = MemberWorkoutExperience.ONE_TO_THREE_YEARS
        )

        // when
        val response = memberRepository.findMemberWithinLocal(baseMember.id!!, member3.id!!, boundingBox, condition)

        // then
        assertThat(response).hasSize(2)
        assertThat(response[0].memberId).isEqualTo(member3.id!!)
        assertThat(response[1].memberId).isEqualTo(member2.id!!)
    }

    @DisplayName("인근 회원 조회 시, 최대 운동 경험 이하의 사람을 조회한다.")
    @Test
    fun findMemberWithinLocalMaxWorkoutExperience() {
        // given
        val baseMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(baseMember)
        val address = Address(
            member = baseMember,
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 50.0,
            lng = 50.0,
            fullAddress = "fullAddress",
            radiusKm = 10.0
        )
        addressRepository.save(address)
        val boundingBox = createBoundingBox(address)


        // BoundingBox(minLat=49.909909909909906, maxLat=50.090090090090094, minLng=49.85984470028284, maxLng=50.14015529971716)

        val member1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member1)
        val inAddress1 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 50.0,
            lng = 50.0,
            fullAddress = "fullAddress"
        )
        val signupRequest1 = TestDataFactory.oAuth2SignupRequest(
            nickname = "nickname1",
            workoutExperience = MemberWorkoutExperience.UNDER_ONE_YEAR,
            workoutStyle = MemberWorkoutStyle.STRENGTH,
            workoutGoal = MemberWorkoutGoal.HEALTH_MAINTENANCE,
            profileImageUrl = "profileImageUrl1",
            address = inAddress1
        )
        oAuth2Service.signup(signupRequest1, member1.id!!)


        val member2 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member2)
        val inAddress2 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 50.00,
            lng = 50.00,
            fullAddress = "fullAddress"
        )
        val signupRequest2 = TestDataFactory.oAuth2SignupRequest(
            nickname = "nickname2",
            workoutExperience = MemberWorkoutExperience.OVER_SEVEN_YEARS,
            workoutStyle = MemberWorkoutStyle.PERFORMANCE,
            workoutGoal = MemberWorkoutGoal.STRENGTH_GAIN,
            profileImageUrl = "profileImageUrl2",
            address = inAddress2
        )
        oAuth2Service.signup(signupRequest2, member2.id!!)


        val member3 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member3)
        val inAddress3 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 50.0,
            lng = 50.0,
            fullAddress = "fullAddress"
        )
        val signupRequest3 = TestDataFactory.oAuth2SignupRequest(
            nickname = "nickname3",
            workoutExperience = MemberWorkoutExperience.ONE_TO_THREE_YEARS,
            workoutStyle = MemberWorkoutStyle.PARTNER,
            workoutGoal = MemberWorkoutGoal.STRESS_RELIEF,
            profileImageUrl = "profileImageUrl3",
            address = inAddress3
        )
        oAuth2Service.signup(signupRequest3, member3.id!!)


        val condition = MemberLocalCondition(
            size = 10,
            page = 1,
            maxWorkoutExperience = MemberWorkoutExperience.FOUR_TO_SIX_YEARS
        )

        // when
        val response = memberRepository.findMemberWithinLocal(baseMember.id!!, member3.id!!, boundingBox, condition)

        // then
        assertThat(response).hasSize(2)
        assertThat(response[0].memberId).isEqualTo(member3.id!!)
        assertThat(response[1].memberId).isEqualTo(member1.id!!)
    }

    @DisplayName("인근 회원 조회 시, 운동 스타일이 지정된 값과 동일한 회원만 조회한다.")
    @Test
    fun findMemberWithinLocalWorkoutStyle() {
        // given
        val baseMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(baseMember)
        val address = Address(
            member = baseMember,
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 50.0,
            lng = 50.0,
            fullAddress = "fullAddress",
            radiusKm = 10.0
        )
        addressRepository.save(address)
        val boundingBox = createBoundingBox(address)

        // BoundingBox(minLat=49.909909909909906, maxLat=50.090090090090094, minLng=49.85984470028284, maxLng=50.14015529971716)

        val member1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member1)
        val inAddress1 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 50.0,
            lng = 50.0,
            fullAddress = "fullAddress"
        )
        val signupRequest1 = TestDataFactory.oAuth2SignupRequest(
            workoutStyle = MemberWorkoutStyle.STRENGTH,
            address = inAddress1
        )
        oAuth2Service.signup(signupRequest1, member1.id!!)


        val member2 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member2)
        val inAddress2 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 50.00,
            lng = 50.00,
            fullAddress = "fullAddress"
        )
        val signupRequest2 = TestDataFactory.oAuth2SignupRequest(
            workoutStyle = MemberWorkoutStyle.PERFORMANCE,
            address = inAddress2
        )
        oAuth2Service.signup(signupRequest2, member2.id!!)


        val member3 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member3)
        val inAddress3 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 50.0,
            lng = 50.0,
            fullAddress = "fullAddress"
        )
        val signupRequest3 = TestDataFactory.oAuth2SignupRequest(
            workoutStyle = MemberWorkoutStyle.PARTNER,
            address = inAddress3
        )
        oAuth2Service.signup(signupRequest3, member3.id!!)


        val condition = MemberLocalCondition(
            size = 10,
            page = 1,
            workoutStyle = listOf(MemberWorkoutStyle.PARTNER, MemberWorkoutStyle.STRENGTH)
        )

        // when
        val response = memberRepository.findMemberWithinLocal(baseMember.id!!, member3.id!!, boundingBox, condition)

        // then
        assertThat(response).hasSize(2)
        assertThat(response[0].memberId).isEqualTo(member3.id!!)
        assertThat(response[1].memberId).isEqualTo(member1.id!!)
    }

    @DisplayName("인근 회원 조회 시, 운동 목표가 지정된 값과 동일한 회원만 조회한다.")
    @Test
    fun findMemberWithinLocalWorkoutGoal() {
        // given
        val baseMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(baseMember)
        val address = Address(
            member = baseMember,
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 50.0,
            lng = 50.0,
            fullAddress = "fullAddress",
            radiusKm = 10.0
        )
        addressRepository.save(address)
        val boundingBox = createBoundingBox(address)


        // BoundingBox(minLat=49.909909909909906, maxLat=50.090090090090094, minLng=49.85984470028284, maxLng=50.14015529971716)

        val member1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member1)
        val inAddress1 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 50.0,
            lng = 50.0,
            fullAddress = "fullAddress"
        )
        val signupRequest1 = TestDataFactory.oAuth2SignupRequest(
            workoutGoal = MemberWorkoutGoal.PERFORMANCE_GOAL,
            address = inAddress1
        )
        oAuth2Service.signup(signupRequest1, member1.id!!)


        val member2 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member2)
        val inAddress2 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 50.00,
            lng = 50.00,
            fullAddress = "fullAddress"
        )
        val signupRequest2 = TestDataFactory.oAuth2SignupRequest(
            workoutGoal = MemberWorkoutGoal.BODY_CORRECTION,
            address = inAddress2
        )
        oAuth2Service.signup(signupRequest2, member2.id!!)


        val member3 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member3)
        val inAddress3 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 49.90,
            lng = 49.85,
            fullAddress = "fullAddress"
        )
        val signupRequest3 = TestDataFactory.oAuth2SignupRequest(
            workoutGoal = MemberWorkoutGoal.STRENGTH_GAIN,
            address = inAddress3
        )
        oAuth2Service.signup(signupRequest3, member3.id!!)


        val condition = MemberLocalCondition(
            size = 10,
            page = 1,
            workoutGoal = listOf(MemberWorkoutGoal.BODY_CORRECTION, MemberWorkoutGoal.PERFORMANCE_GOAL)
        )

        // when
        val response = memberRepository.findMemberWithinLocal(baseMember.id!!, member3.id!!, boundingBox, condition)

        // then
        assertThat(response).hasSize(2)
        assertThat(response[0].memberId).isEqualTo(member1.id!!)
        assertThat(response[1].memberId).isEqualTo(member2.id!!)
    }

    @DisplayName("인근 회원 조회 시, 나이대가 지정된 범위의 회원만 조회한다.")
    @Test
    fun findMemberWithinLocalAge() {
        // given
        val baseMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(baseMember)
        val address = Address(
            member = baseMember,
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 50.0,
            lng = 50.0,
            fullAddress = "fullAddress",
            radiusKm = 10.0
        )
        addressRepository.save(address)
        val boundingBox = createBoundingBox(address)


        val member1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member1)
        val inAddress1 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 50.0,
            lng = 50.0,
            fullAddress = "fullAddress"
        )
        val koreanAgeTwentiesMidExceed = time.nowLocalDate.minusYears(Age.TWENTIES_MID.max.toLong())
        val signupRequest1 = TestDataFactory.oAuth2SignupRequest(
            birthday = koreanAgeTwentiesMidExceed,
            address = inAddress1
        )
        oAuth2Service.signup(signupRequest1, member1.id!!)


        val member2 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member2)
        val inAddress2 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 50.00,
            lng = 50.00,
            fullAddress = "fullAddress"
        )
        val koreanAgeTwentiesMid = time.nowLocalDate.minusYears(Age.TWENTIES_MID.max.toLong() - 1)
        val signupRequest2 = TestDataFactory.oAuth2SignupRequest(
            birthday = koreanAgeTwentiesMid,
            address = inAddress2
        )
        oAuth2Service.signup(signupRequest2, member2.id!!)


        val member3 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member3)
        val inAddress3 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 50.0,
            lng = 50.0,
            fullAddress = "fullAddress"
        )
        val koreanAgeFortiesMid = time.nowLocalDate.minusYears(Age.FORTIES_MID.min.toLong())
        val signupRequest3 = TestDataFactory.oAuth2SignupRequest(
            birthday = koreanAgeFortiesMid,
            address = inAddress3
        )
        oAuth2Service.signup(signupRequest3, member3.id!!)


        val condition = MemberLocalCondition(
            size = 10,
            page = 1,
            age = listOf(Age.FORTIES_MID, Age.TWENTIES_MID)
        )

        // when
        val response = memberRepository.findMemberWithinLocal(baseMember.id!!, member3.id!!, boundingBox, condition)

        // then
        assertThat(response).hasSize(2)
        assertThat(response[0].memberId).isEqualTo(member3.id!!)
        assertThat(response[1].memberId).isEqualTo(member2.id!!)
    }


    @DisplayName("인근 회원 조회 시, 최소키 이상의 회원만 조회한다.")
    @Test
    fun findMemberWithinLocalMinHeight() {
        // given
        val baseMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(baseMember)
        val address = Address(
            member = baseMember,
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 50.0,
            lng = 50.0,
            fullAddress = "fullAddress",
            radiusKm = 10.0
        )
        addressRepository.save(address)
        val boundingBox = createBoundingBox(address)


        // BoundingBox(minLat=49.909909909909906, maxLat=50.090090090090094, minLng=49.85984470028284, maxLng=50.14015529971716)

        val member1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member1)
        val inAddress1 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 49.9,
            lng = 50.14,
            fullAddress = "fullAddress"
        )
        val signupRequest1 = TestDataFactory.oAuth2SignupRequest(
            height = 169,
            address = inAddress1
        )
        oAuth2Service.signup(signupRequest1, member1.id!!)


        val member2 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member2)
        val inAddress2 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 50.00,
            lng = 50.00,
            fullAddress = "fullAddress"
        )
        val signupRequest2 = TestDataFactory.oAuth2SignupRequest(
            height = 170,
            address = inAddress2
        )
        oAuth2Service.signup(signupRequest2, member2.id!!)

        val condition = MemberLocalCondition(
            size = 10,
            page = 1,
            minHeight = 170
        )

        // when
        val response = memberRepository.findMemberWithinLocal(baseMember.id!!, member2.id!!, boundingBox, condition)

        // then
        assertThat(response).hasSize(1)
        assertThat(response[0].memberId).isEqualTo(member2.id!!)
    }

    @DisplayName("인근 회원 조회 시, 최대키 이하의 회원만 조회한다.")
    @Test
    fun findMemberWithinLocalMaxHeight() {
        // given
        val baseMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(baseMember)
        val address = Address(
            member = baseMember,
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 50.0,
            lng = 50.0,
            fullAddress = "fullAddress",
            radiusKm = 10.0
        )
        addressRepository.save(address)
        val boundingBox = createBoundingBox(address)


        // BoundingBox(minLat=49.909909909909906, maxLat=50.090090090090094, minLng=49.85984470028284, maxLng=50.14015529971716)

        val member1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member1)
        val inAddress1 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 50.0,
            lng = 50.00,
            fullAddress = "fullAddress"
        )
        val signupRequest1 = TestDataFactory.oAuth2SignupRequest(
            height = 180,
            address = inAddress1
        )
        oAuth2Service.signup(signupRequest1, member1.id!!)


        val member2 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member2)
        val inAddress2 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 50.00,
            lng = 50.00,
            fullAddress = "fullAddress"
        )
        val signupRequest2 = TestDataFactory.oAuth2SignupRequest(
            height = 181,
            address = inAddress2
        )
        oAuth2Service.signup(signupRequest2, member2.id!!)

        val condition = MemberLocalCondition(
            size = 10,
            page = 1,
            maxHeight = 180
        )

        // when
        val response = memberRepository.findMemberWithinLocal(baseMember.id!!, member2.id!!, boundingBox, condition)

        // then
        assertThat(response).hasSize(1)
        assertThat(response[0].memberId).isEqualTo(member1.id!!)
    }

    @DisplayName("인근 회원 조회 시, 최소 체중 이상의 회원만 조회한다.")
    @Test
    fun findMemberWithinLocalMinWeight() {
        // given
        val baseMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(baseMember)
        val address = Address(
            member = baseMember,
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 50.0,
            lng = 50.0,
            fullAddress = "fullAddress",
            radiusKm = 10.0
        )
        addressRepository.save(address)
        val boundingBox = createBoundingBox(address)


        // BoundingBox(minLat=49.909909909909906, maxLat=50.090090090090094, minLng=49.85984470028284, maxLng=50.14015529971716)

        val member1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member1)
        val inAddress1 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 49.9,
            lng = 50.14,
            fullAddress = "fullAddress"
        )
        val signupRequest1 = TestDataFactory.oAuth2SignupRequest(
            weight = 49,
            address = inAddress1
        )
        oAuth2Service.signup(signupRequest1, member1.id!!)


        val member2 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member2)
        val inAddress2 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 50.00,
            lng = 50.00,
            fullAddress = "fullAddress"
        )
        val signupRequest2 = TestDataFactory.oAuth2SignupRequest(
            weight = 50,
            address = inAddress2
        )
        oAuth2Service.signup(signupRequest2, member2.id!!)

        val condition = MemberLocalCondition(
            size = 10,
            page = 1,
            minWeight = 50
        )

        // when
        val response = memberRepository.findMemberWithinLocal(baseMember.id!!, member2.id!!, boundingBox, condition)

        // then
        assertThat(response).hasSize(1)
        assertThat(response[0].memberId).isEqualTo(member2.id!!)
    }

    @DisplayName("인근 회원 조회 시, 최대 체중 이하의 회원만 조회한다.")
    @Test
    fun findMemberWithinLocalMaxWeight() {
        // given
        val baseMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(baseMember)
        val address = Address(
            member = baseMember,
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 50.0,
            lng = 50.0,
            fullAddress = "fullAddress",
            radiusKm = 10.0
        )
        addressRepository.save(address)
        val boundingBox = createBoundingBox(address)


        // BoundingBox(minLat=49.909909909909906, maxLat=50.090090090090094, minLng=49.85984470028284, maxLng=50.14015529971716)

        val member1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member1)
        val inAddress1 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 49.9,
            lng = 50.14,
            fullAddress = "fullAddress"
        )
        val signupRequest1 = TestDataFactory.oAuth2SignupRequest(
            weight = 81,
            address = inAddress1
        )
        oAuth2Service.signup(signupRequest1, member1.id!!)


        val member2 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member2)
        val inAddress2 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 50.00,
            lng = 50.00,
            fullAddress = "fullAddress"
        )
        val signupRequest2 = TestDataFactory.oAuth2SignupRequest(
            weight = 80,
            address = inAddress2
        )
        oAuth2Service.signup(signupRequest2, member2.id!!)

        val condition = MemberLocalCondition(
            size = 10,
            page = 1,
            maxWeight = 80
        )

        // when
        val response = memberRepository.findMemberWithinLocal(baseMember.id!!, member2.id!!, boundingBox, condition)

        // then
        assertThat(response).hasSize(1)
        assertThat(response[0].memberId).isEqualTo(member2.id!!)
    }

    @DisplayName("인근 회원 조회 시, 본인은 조회되지 않는다.")
    @Test
    fun findMemberWithinLocalNotMe() {
        // given
        val baseMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(baseMember)
        val baseAddress = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 50.0,
            lng = 50.0,
            fullAddress = "fullAddress",
        )
        val baseSignupRequest = TestDataFactory.oAuth2SignupRequest(
            weight = 81,
            address = baseAddress
        )
        oAuth2Service.signup(baseSignupRequest, baseMember.id!!)
        val findAddress = addressRepository.findByMemberIdAndDeletedAtIsNull(baseMember.id!!)
        findAddress!!.radiusKm = 10.0

        val boundingBox = createBoundingBox(findAddress)


        // BoundingBox(minLat=49.909909909909906, maxLat=50.090090090090094, minLng=49.85984470028284, maxLng=50.14015529971716)

        val member1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member1)
        val inAddress1 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 49.9,
            lng = 50.14,
            fullAddress = "fullAddress"
        )
        val signupRequest1 = TestDataFactory.oAuth2SignupRequest(
            weight = 81,
            address = inAddress1
        )
        oAuth2Service.signup(signupRequest1, member1.id!!)


        val member2 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member2)
        val inAddress2 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 50.00,
            lng = 50.00,
            fullAddress = "fullAddress"
        )
        val signupRequest2 = TestDataFactory.oAuth2SignupRequest(
            weight = 80,
            address = inAddress2
        )
        oAuth2Service.signup(signupRequest2, member2.id!!)

        val condition = MemberLocalCondition(
            size = 10,
            page = 1,
            maxWeight = 80
        )

        // when
        val response = memberRepository.findMemberWithinLocal(baseMember.id!!, member2.id!!,boundingBox, condition)

        // then
        assertThat(response).hasSize(1)
        assertThat(response[0].memberId).isEqualTo(member2.id!!)
    }


    private fun signupMember(siDo : AddressSiDo) : Member{
        val member = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member)
        val address = AddressCreateServiceRequest(
            siDo = siDo,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 50.00,
            lng = 50.00,
            fullAddress = "fullAddress"
        )
        val signupRequest = TestDataFactory.oAuth2SignupRequest(
            address = address
        )
        oAuth2Service.signup(signupRequest, member.id!!)
        return member
    }

    @DisplayName("각 회원 id를 제외한 서울 회원들을 찾는다.")
    @Test
    fun findMemberWithinSeoulByNotMemberIds() {
        // given
        val member1 = signupMember(AddressSiDo.SEOUL)
        val member2 = signupMember(AddressSiDo.SEOUL)
        val member3 = signupMember(AddressSiDo.SEOUL)
        val member4 = signupMember(AddressSiDo.SEOUL)
        val member5 = signupMember(AddressSiDo.BUSAN)

        val meMemberId = member1.id!!
        val size = 10
        val memberIds = listOf(member2.id!!, member3.id!!)

        // when
        val response = memberRepository.findMemberWithinSeoulByNotMemberIds(
            meMemberId = meMemberId,
            size = size,
            memberIds = memberIds
        )

        // then
        assertThat(response).hasSize(1)
        assertThat(response[0].memberId).isEqualTo(member4.id!!)
    }



    @DisplayName("회원의 최대 id를 조회한다.")
    @Test
    fun findMemberMaxId() {
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

        // when
        val memberMaxId = memberRepository.findMemberMaxId()

        // then
        assertThat(memberMaxId).isEqualTo(memberMaxId)
    }

    @DisplayName("추천 핏버디 - 운동 경력/스타일/목표가 2개 이상 맞는 회원을 조회한다.")
    @Test
    fun findMemberWithinRecommendation() {
        // given
        val me = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(me)
        val signupRequest = TestDataFactory.oAuth2SignupRequest(
            workoutExperience = MemberWorkoutExperience.FOUR_TO_SIX_YEARS,
            workoutStyle = MemberWorkoutStyle.PARTNER,
            workoutGoal = MemberWorkoutGoal.STRENGTH_GAIN
        )
        oAuth2Service.signup(signupRequest, me.id!!)

        val matchMember1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(matchMember1)
        val signupRequest2 = TestDataFactory.oAuth2SignupRequest(
            nickname = "update1",
            workoutExperience = MemberWorkoutExperience.JUST_STARTED,
            workoutStyle = MemberWorkoutStyle.PARTNER,
            workoutGoal = MemberWorkoutGoal.STRENGTH_GAIN,
            workoutImageUrls = listOf("updateMember1Workout1", "updateMember1Workout2")
        )
        oAuth2Service.signup(signupRequest2, matchMember1.id!!)

        val matchMember2 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(matchMember2)
        val signupRequest3 = TestDataFactory.oAuth2SignupRequest(
            nickname = "update2",
            workoutExperience = MemberWorkoutExperience.FOUR_TO_SIX_YEARS,
            workoutStyle = MemberWorkoutStyle.TENSION,
            workoutGoal = MemberWorkoutGoal.STRENGTH_GAIN,
            workoutImageUrls = listOf("updateMember2Workout1", "updateMember2Workout2")
        )
        oAuth2Service.signup(signupRequest3, matchMember2.id!!)

        val matchMember3 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(matchMember3)
        val signupRequest4 = TestDataFactory.oAuth2SignupRequest(
            nickname = "update3",
            workoutExperience = MemberWorkoutExperience.OVER_SEVEN_YEARS,
            workoutStyle = MemberWorkoutStyle.PARTNER,
            workoutGoal = MemberWorkoutGoal.STRENGTH_GAIN,
            workoutImageUrls = listOf("updateMember3Workout1", "updateMember3Workout2")
        )
        oAuth2Service.signup(signupRequest4, matchMember3.id!!)

        val notMatchMember1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(notMatchMember1)
        val signupRequest5 = TestDataFactory.oAuth2SignupRequest(
            workoutExperience = MemberWorkoutExperience.JUST_STARTED,
            workoutStyle = MemberWorkoutStyle.PERFORMANCE,
            workoutGoal = MemberWorkoutGoal.STRENGTH_GAIN
        )
        oAuth2Service.signup(signupRequest5, notMatchMember1.id!!)


        // when
        val response = memberRepository.findMemberWithinRecommendation(
            member = me,
            randomMemberId = 1,
            size = 10
        )

        // then
        assertThat(response).hasSize(3)
        assertThat(response)
            .extracting("memberId", "nickname", "workoutImageUrl")
            .containsExactlyInAnyOrder(
                tuple(matchMember1.id!!, "update1", "updateMember1Workout1"),
                tuple(matchMember2.id!!, "update2", "updateMember2Workout1"),
                tuple(matchMember3.id!!, "update3", "updateMember3Workout1"),
            )
    }

    @DisplayName("추천 핏버디 조회 시, 랜덤 시작 지점에서 size만큼 부족하면 처음부터 이어서 조회한다.")
    @Test
    fun findMemberWithinRecommendationRandomMemberIdMissSearch() {
        // given
        val me = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(me)
        val signupRequest = TestDataFactory.oAuth2SignupRequest(
            workoutExperience = MemberWorkoutExperience.FOUR_TO_SIX_YEARS,
            workoutStyle = MemberWorkoutStyle.PARTNER,
            workoutGoal = MemberWorkoutGoal.STRENGTH_GAIN
        )
        oAuth2Service.signup(signupRequest, me.id!!)

        val matchMember1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(matchMember1)
        val signupRequest2 = TestDataFactory.oAuth2SignupRequest(
            nickname = "update1",
            workoutExperience = MemberWorkoutExperience.JUST_STARTED,
            workoutStyle = MemberWorkoutStyle.PARTNER,
            workoutGoal = MemberWorkoutGoal.STRENGTH_GAIN,
            workoutImageUrls = listOf("updateMember1Workout1", "updateMember1Workout2")
        )
        oAuth2Service.signup(signupRequest2, matchMember1.id!!)

        val matchMember2 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(matchMember2)
        val signupRequest3 = TestDataFactory.oAuth2SignupRequest(
            nickname = "update2",
            workoutExperience = MemberWorkoutExperience.FOUR_TO_SIX_YEARS,
            workoutStyle = MemberWorkoutStyle.TENSION,
            workoutGoal = MemberWorkoutGoal.STRENGTH_GAIN,
            workoutImageUrls = listOf("updateMember2Workout1", "updateMember2Workout2")
        )
        oAuth2Service.signup(signupRequest3, matchMember2.id!!)

        val matchMember3 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(matchMember3)
        val signupRequest4 = TestDataFactory.oAuth2SignupRequest(
            nickname = "update3",
            workoutExperience = MemberWorkoutExperience.OVER_SEVEN_YEARS,
            workoutStyle = MemberWorkoutStyle.PARTNER,
            workoutGoal = MemberWorkoutGoal.STRENGTH_GAIN,
            workoutImageUrls = listOf("updateMember3Workout1", "updateMember3Workout2")
        )
        oAuth2Service.signup(signupRequest4, matchMember3.id!!)

        val notMatchMember1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(notMatchMember1)
        val signupRequest5 = TestDataFactory.oAuth2SignupRequest(
            workoutExperience = MemberWorkoutExperience.JUST_STARTED,
            workoutStyle = MemberWorkoutStyle.PERFORMANCE,
            workoutGoal = MemberWorkoutGoal.STRENGTH_GAIN
        )
        oAuth2Service.signup(signupRequest5, notMatchMember1.id!!)

        // when
        val response = memberRepository.findMemberWithinRecommendation(
            member = me,
            randomMemberId = matchMember3.id!!,
            size = 10
        )

        // then
        assertThat(response).hasSize(3)
        assertThat(response)
            .extracting("memberId", "nickname", "workoutImageUrl")
            .containsExactly(
                tuple(matchMember3.id!!, "update3", "updateMember3Workout1"),
                tuple(matchMember1.id!!, "update1", "updateMember1Workout1"),
                tuple(matchMember2.id!!, "update2", "updateMember2Workout1"),
            )
    }

    @DisplayName("추천 핏버디는 서울 인원만 조회")
    @Test
    fun findMemberWithinRecommendationOnlySeoul() {
        // given
        val me = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(me)
        val signupRequest = TestDataFactory.oAuth2SignupRequest(
            workoutExperience = MemberWorkoutExperience.FOUR_TO_SIX_YEARS,
            workoutStyle = MemberWorkoutStyle.PARTNER,
            workoutGoal = MemberWorkoutGoal.STRENGTH_GAIN
        )
        oAuth2Service.signup(signupRequest, me.id!!)

        val matchMember1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(matchMember1)
        val signupRequest1 = TestDataFactory.oAuth2SignupRequest(
            nickname = "update1",
            workoutExperience = MemberWorkoutExperience.JUST_STARTED,
            workoutStyle = MemberWorkoutStyle.PARTNER,
            workoutGoal = MemberWorkoutGoal.STRENGTH_GAIN,
            workoutImageUrls = listOf("updateMember1Workout1", "updateMember1Workout2")
        )
        oAuth2Service.signup(signupRequest1, matchMember1.id!!)

        val notSeoulMember = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(notSeoulMember)
        val address2 = AddressCreateServiceRequest(
            siDo = AddressSiDo.BUSAN,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 50.00,
            lng = 50.00,
            fullAddress = "fullAddress"
        )
        val signupRequest2 = TestDataFactory.oAuth2SignupRequest(
            nickname = "update2",
            workoutExperience = MemberWorkoutExperience.FOUR_TO_SIX_YEARS,
            workoutStyle = MemberWorkoutStyle.PARTNER,
            workoutGoal = MemberWorkoutGoal.STRENGTH_GAIN,
            workoutImageUrls = listOf("updateMember2Workout1", "updateMember2Workout2"),
            address = address2
        )
        oAuth2Service.signup(signupRequest2, notSeoulMember.id!!)

        // when
        val response = memberRepository.findMemberWithinRecommendation(
            member = me,
            randomMemberId = matchMember1.id!!,
            size = 10
        )

        // then
        assertThat(response).hasSize(1)
        assertThat(response)
            .extracting("memberId", "nickname", "workoutImageUrl")
            .containsExactly(
                tuple(matchMember1.id!!, "update1", "updateMember1Workout1"),
            )
    }

    @DisplayName("추천 핏버디에서 요구 개수만큼 조회를 못하면, 서울 인원으로 조회")
    @Test
    fun findMemberByNotMemberIdsWithinRecommendationsAndSeoul() {
        // given
        val me = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(me)
        val signupRequest = TestDataFactory.oAuth2SignupRequest(
            workoutExperience = MemberWorkoutExperience.FOUR_TO_SIX_YEARS,
            workoutStyle = MemberWorkoutStyle.PARTNER,
            workoutGoal = MemberWorkoutGoal.STRENGTH_GAIN
        )
        oAuth2Service.signup(signupRequest, me.id!!)

        val matchMember1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(matchMember1)
        val signupRequest2 = TestDataFactory.oAuth2SignupRequest(
            nickname = "update2",
            workoutExperience = MemberWorkoutExperience.JUST_STARTED,
            workoutStyle = MemberWorkoutStyle.PARTNER,
            workoutGoal = MemberWorkoutGoal.STRENGTH_GAIN,
            workoutImageUrls = listOf("updateMember1Workout1", "updateMember1Workout2")
        )
        oAuth2Service.signup(signupRequest2, matchMember1.id!!)


        val notMatchMember1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(notMatchMember1)
        val address2 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "강남구",
            eupMyeonDong = "역삼동",
            lat = 37.4979,
            lng = 127.0276,
            fullAddress = "서울특별시 강남구 테헤란로 123"
        )
        val signupRequest3 = TestDataFactory.oAuth2SignupRequest(
            nickname = "update3",
            workoutExperience = MemberWorkoutExperience.OVER_SEVEN_YEARS,
            workoutStyle = MemberWorkoutStyle.BALANCE,
            workoutGoal = MemberWorkoutGoal.STRESS_RELIEF,
            workoutImageUrls = listOf("updateMember3Workout1", "updateMember3Workout2"),
            address = address2
        )
        oAuth2Service.signup(signupRequest3, notMatchMember1.id!!)

        val memberIds = listOf(
            matchMember1.id!!,
        )

        // when
        val response = memberRepository.findMemberByNotMemberIdsWithinRecommendationsAndSeoul(
            memberId = me.id!!,
            memberIds = memberIds,
            size = 10,
        )

        // then
        assertThat(response).hasSize(1)
        assertThat(response)
            .extracting("memberId", "nickname", "workoutImageUrl")
            .containsExactly(
                tuple(notMatchMember1.id!!, "update3", "updateMember3Workout1"),
            )
    }

    @DisplayName("추천 핏버디의 나머지 서울 인원 조회 시, 서울 인원이 아니면 조회하지않는다.")
    @Test
    fun findMemberByNotMemberIdsWithinRecommendationsAndNotSeoul() {
        // given
        val me = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(me)
        val signupRequest = TestDataFactory.oAuth2SignupRequest(
            workoutExperience = MemberWorkoutExperience.FOUR_TO_SIX_YEARS,
            workoutStyle = MemberWorkoutStyle.PARTNER,
            workoutGoal = MemberWorkoutGoal.STRENGTH_GAIN
        )
        oAuth2Service.signup(signupRequest, me.id!!)

        val matchMember1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(matchMember1)
        val signupRequest2 = TestDataFactory.oAuth2SignupRequest(
            nickname = "update2",
            workoutExperience = MemberWorkoutExperience.JUST_STARTED,
            workoutStyle = MemberWorkoutStyle.PARTNER,
            workoutGoal = MemberWorkoutGoal.STRENGTH_GAIN,
            workoutImageUrls = listOf("updateMember1Workout1", "updateMember1Workout2")
        )
        oAuth2Service.signup(signupRequest2, matchMember1.id!!)


        val notMatchMember1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(notMatchMember1)
        val address2 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "강남구",
            eupMyeonDong = "역삼동",
            lat = 37.4979,
            lng = 127.0276,
            fullAddress = "서울특별시 강남구 테헤란로 123"
        )
        val signupRequest3 = TestDataFactory.oAuth2SignupRequest(
            nickname = "update3",
            workoutExperience = MemberWorkoutExperience.OVER_SEVEN_YEARS,
            workoutStyle = MemberWorkoutStyle.BALANCE,
            workoutGoal = MemberWorkoutGoal.STRESS_RELIEF,
            workoutImageUrls = listOf("updateMember3Workout1", "updateMember3Workout2"),
            address = address2
        )
        oAuth2Service.signup(signupRequest3, notMatchMember1.id!!)

        val notMatchMember2 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(notMatchMember2)
        val address3 = AddressCreateServiceRequest(
            siDo = AddressSiDo.BUSAN,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 37.4979,
            lng = 127.0276,
            fullAddress = "fullAddress"
        )
        val signupRequest4 = TestDataFactory.oAuth2SignupRequest(
            nickname = "update4",
            workoutExperience = MemberWorkoutExperience.OVER_SEVEN_YEARS,
            workoutStyle = MemberWorkoutStyle.BALANCE,
            workoutGoal = MemberWorkoutGoal.STRESS_RELIEF,
            workoutImageUrls = listOf("updateMember4Workout1", "updateMember4Workout2"),
            address = address3
        )
        oAuth2Service.signup(signupRequest4, notMatchMember2.id!!)

        val memberIds = listOf(
            matchMember1.id!!,
        )

        // when
        val response = memberRepository.findMemberByNotMemberIdsWithinRecommendationsAndSeoul(
            memberId = me.id!!,
            memberIds = memberIds,
            size = 10,
        )

        // then
        assertThat(response).hasSize(1)
        assertThat(response)
            .extracting("memberId", "nickname", "workoutImageUrl")
            .containsExactly(
                tuple(notMatchMember1.id!!, "update3", "updateMember3Workout1"),
            )
    }


    @DisplayName("해당 채팅방에 있는 회원들을 모두 조회한다.")
    @Test
    fun findMemberByPrivateChatRoomId() {
        // given
        val me = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(me)
        val signupRequest1 = TestDataFactory.oAuth2SignupRequest()
        oAuth2Service.signup(signupRequest1, me.id!!)

        val other = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(other)
        val signupRequest2 = TestDataFactory.oAuth2SignupRequest(
            nickname = "updateNick",
            profileImageUrl = "updateProfile"
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


        val other2 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(other2)
        val signupRequest3 = TestDataFactory.oAuth2SignupRequest(
            nickname = "updateNick",
            profileImageUrl = "updateProfile"
        )
        oAuth2Service.signup(signupRequest3, other2.id!!)

        val chatRoom2 = chatRoomRepository.save(ChatRoom(ChatRoomType.PRIVATE))

        val chatParticipant3 = ChatParticipant(
            chatRoom2,
            me
        )
        val chatParticipant4 = ChatParticipant(
            chatRoom2,
            other2
        )
        chatParticipantRepository.save(chatParticipant3)
        chatParticipantRepository.save(chatParticipant4)

        // when
        val response = memberRepository.findMemberByPrivateChatRoomId(me.id!!, chatRoom.id!!)

        // then
        assertThat(response!!.me)
            .extracting("memberId", "nickname", "profileImageUrl")
            .contains(me.id!!, signupRequest1.nickname, signupRequest1.profileImageUrl)

        assertThat(response.other)
            .extracting("memberId", "nickname", "profileImageUrl")
            .contains(other.id!!, signupRequest2.nickname, signupRequest2.profileImageUrl)
    }



}