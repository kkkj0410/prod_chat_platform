package kr.co.fitview.api.app.domain.member.service

import jakarta.persistence.EntityManager
import jakarta.persistence.EntityNotFoundException
import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.address.dto.request.AddressCreateServiceRequest
import kr.co.fitview.api.app.domain.address.entity.Address
import kr.co.fitview.api.app.domain.address.entity.enums.AddressSiDo
import kr.co.fitview.api.app.domain.address.repository.AddressRepository
import kr.co.fitview.api.app.domain.image.repository.MemberImageRepository
import kr.co.fitview.api.app.domain.member.condition.MemberLocalCondition
import kr.co.fitview.api.app.domain.member.dto.request.Age
import kr.co.fitview.api.app.domain.member.dto.request.MemberUpdateServiceRequest
import kr.co.fitview.api.app.domain.member.dto.response.enums.ProfileWorkoutPartnerStatus
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.entity.WorkoutTime
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle
import kr.co.fitview.api.app.domain.member.entity.enums.WorkoutTimeName
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.member.repository.WorkoutTimeRepository
import kr.co.fitview.api.app.domain.oauth2.dto.request.OAuth2SignupServiceRequest
import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.global.entity.Gender
import kr.co.fitview.api.app.global.entity.OAuth2Provider
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.global.GlobalErrorCode
import kr.co.fitview.api.app.global.exception.error.member.MemberErrorCode
import kr.co.fitview.api.app.global.random.RandomCustom
import kr.co.fitview.api.app.global.util.TestDataFactory
import org.assertj.core.api.Assertions.*
import org.assertj.core.api.ThrowingConsumer
import org.hibernate.proxy.HibernateProxy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate

class MemberServiceTest @Autowired constructor(
    val memberService : MemberService,
    val memberRepository : MemberRepository,
    val workoutTimeRepository : WorkoutTimeRepository,
    val addressRepository: AddressRepository,
    val oAuth2Service : OAuth2Service,
    val memberImageRepository : MemberImageRepository,
    val em : EntityManager,
    val randomCustom : RandomCustom
) : IntegrationTestSupport() {

    @DisplayName("사용자 정보를 저장한다")
    @Test
    fun saveMember() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )

        // when
        val savedMember = memberService.addMember(member);

        // then
        assertThat(savedMember.id).isNotNull()
        assertThat(savedMember)
            .extracting("email", "password", "role")
            .contains(member.email, member.password, Role.USER)
    }


    @DisplayName("사용자 정보를 저장할 때 로그인 id가 중복될 수 없다.")
    @Test
    fun saveMemberDuplicateEmail() {
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

        memberService.addMember(member1);

        // when & then
        assertThatThrownBy {
            memberService.addMember(member2);
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(MemberErrorCode.MEMBER_DUPLICATE_EMAIL)
            })
    }

   @DisplayName("로그인 정보로 회원을 찾는다.")
   @Test
   fun findMemberFromId() {
       // given
       val member = Member(
           email = "email",
           password = "password",
           role = Role.USER,
       )
       memberRepository.save(member)

       // when
       val findMember = memberService.findMemberFromEmail(member.email!!)

       // then
       assertThat(findMember)
           .extracting("email", "password", "role")
           .contains(member.email, member.password, Role.USER)

   }

    @DisplayName("존재하지 않는 회원이면 로그인 정보로 회원을 찾을 수 없다.")
    @Test
    fun findMemberFromWithoutMemberLoginId() {
        // given
        val email = "email"

        // when
        val findMember = memberService.findMemberFromEmail(email)

        // then
        assertThat(findMember).isNull()
    }

    @DisplayName("회원 id로 회원을 찾는다.")
    @Test
    fun findMemberFromMemberIdLoginId() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(member)

        // when
        val findMember = memberService.findMemberFromId(member.id!!)

        // then
        assertThat(findMember)
            .extracting("email", "password", "role")
            .contains(member.email, member.password, Role.USER)

    }

    @DisplayName("존재하지 않는 회원이면 회원 id로 회원을 찾을 수 없다.")
    @Test
    fun findMemberFromMemberIdWithoutMemberLoginId() {
        // given
        val memberId = 100L

        // when
        val findMember = memberService.findMemberFromId(memberId)

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


    @DisplayName("회원 id로 회원 정보를 조회한다.")
    @Test
    fun findMemberProfile() {
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
        val response = memberService.findMemberProfile(savedMember.id!!)

        // then
        assertThat(response).isNotNull
        response.let {
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

    @DisplayName("회원 정보가 없으면 회원 고유 id로 사용자 정보를 조회할 수 없다.")
    @Test
    fun findMemberProfileWithoutMember() {
        // given
        val memberId = 100L

        // when & then
        assertThatThrownBy {
            memberService.findMemberProfile(memberId)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(GlobalErrorCode.ENTITY_NOT_FOUND)
            })
    }


    @DisplayName("회원이 존재하면 조회한다.")
    @Test
    fun findMemberOrElseThrow() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(member)

        // when
        val findMember = memberService.findMemberOrElseThrow(member.id!!)

        // then
        assertThat(findMember)
            .extracting("email", "password", "role")
            .contains(member.email, member.password, Role.USER)
    }

    @DisplayName("회원이 존재하지 않으면 조회에 실패한다.")
    @Test
    fun findMemberOrElseThrowWithoutMember() {
        // given
        val memberId = 100L


        // when & then
        assertThatThrownBy {
            memberService.findMemberOrElseThrow(memberId)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND)
            })
    }


    @DisplayName("소셜 로그인 회원이 존재하면 해당 회원을 조회한다.")
    @Test
    fun findMemberFromProviderId() {
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
        val findMember = memberService.findMemberFromProviderId(savedMember.providerId!!)

        // then
        assertThat(savedMember.id).isNotNull()
        assertThat(findMember)
            .extracting("email", "password", "role", "provider", "providerId")
            .contains(savedMember.email, savedMember.password, savedMember.role, savedMember.provider, savedMember.providerId)
    }

    @DisplayName("소셜 로그인 회원이 존재하지 않으면 해당 회원 조회에 실패한다.")
    @Test
    fun findMemberFromProviderIdWithoutMember() {
        // given
        val providerId = "providerId"

        // when
        val findMember = memberService.findMemberFromProviderId(providerId)

        // then
        assertThat(findMember).isNull()
    }


    @DisplayName("회원은 선호 운동시간을 기록하고, 회원의 전체 선호운동시간을 반환한다.")
    @Test
    fun addWorkoutTimes() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val savedMember = memberRepository.save(member)

        val workoutTimeNames = listOf(
            WorkoutTimeName.WEEKDAY_DAWN,
            WorkoutTimeName.WEEKEND_AFTERNOON
        )

        // when
        val workoutTimes = memberService.addWorkoutTimes(savedMember, workoutTimeNames)

        // then
        assertThat(workoutTimes)
            .allSatisfy { workoutDay ->
                assertThat(workoutDay.id).isNotNull()
            }

        assertThat(workoutTimes)
            .extracting("member", "name")
            .containsExactlyInAnyOrder(
                tuple(savedMember, WorkoutTimeName.WEEKDAY_DAWN),
                tuple(savedMember, WorkoutTimeName.WEEKEND_AFTERNOON),
            )

    }

    @DisplayName("회원이 선호 운동시간을 기록하는데, 이미 DB에 있으면 삭제하고 새로 저장한다.")
    @Test
    fun addWorkoutTimesDuplicatedWorkoutTime() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val savedMember = memberRepository.save(member)

        val workoutTimes = listOf(
            WorkoutTime(
                savedMember,
                WorkoutTimeName.WEEKDAY_DAWN
            ),
            WorkoutTime(
                savedMember,
                WorkoutTimeName.WEEKDAY_EVENING
            )
        )
        workoutTimeRepository.saveAll(workoutTimes)

        val workoutTimeNames = listOf(
            WorkoutTimeName.WEEKDAY_DAWN,
            WorkoutTimeName.WEEKDAY_AFTERNOON
        )

        // when
        val returnWorkoutTimes = memberService.addWorkoutTimes(savedMember, workoutTimeNames)

        // then
        assertThat(returnWorkoutTimes)
            .allSatisfy { workoutTime ->
                assertThat(workoutTime.id).isNotNull()
            }

        assertThat(returnWorkoutTimes)
            .extracting("member", "name")
            .containsExactlyInAnyOrder(
                tuple(savedMember, WorkoutTimeName.WEEKDAY_DAWN),
                tuple(savedMember, WorkoutTimeName.WEEKDAY_AFTERNOON),
            )

        assertThat(returnWorkoutTimes)
            .extracting("name")
            .doesNotHaveDuplicates()

        val findWorkoutTimes = workoutTimeRepository.findAllByMemberIdAndDeletedAtIsNull(savedMember.id!!)

        assertThat(findWorkoutTimes)
            .allSatisfy { workoutTime ->
                assertThat(workoutTime.id).isNotNull()
            }

        assertThat(findWorkoutTimes)
            .extracting("member", "name")
            .containsExactlyInAnyOrder(
                tuple(savedMember, WorkoutTimeName.WEEKDAY_DAWN),
                tuple(savedMember, WorkoutTimeName.WEEKDAY_AFTERNOON),
            )

        assertThat(findWorkoutTimes)
            .extracting("name")
            .doesNotHaveDuplicates()
    }

    @DisplayName("소셜 회원을 저장한다.")
    @Test
    fun addMemberByOAuth2() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE,
            providerId = "1234"
        )

        // when
        val savedMember = memberService.addMemberByOAuth2(member)

        // then
        assertThat(savedMember)
            .extracting("email", "password", "role", "provider", "providerId")
            .contains(member.email, member.password, member.role, member.provider, member.providerId)

    }

    @DisplayName("소셜 회원의 소셜 id가 중복되면 저장하지 않는다.")
    @Test
    fun addMemberByOAuth2DuplicatedProviderId() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE,
            providerId = "1234"
        )
        memberRepository.save(member)


        // when & then
        assertThatThrownBy {
            memberService.addMemberByOAuth2(member)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(MemberErrorCode.MEMBER_DUPLICATE_PROVIDER)
            })
    }

    @DisplayName("소셜 회원은 이메일이 중복되더라도 소셜 id가 다르다면 각 회원을 모두 저장한다.")
    @Test
    fun addMemberByOAuth2DuplicatedEmail() {
        // given
        val email = "email"
        val member1 = Member(
            email = email,
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE,
            providerId = "1234"
        )
        val member2= Member(
            email = email,
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE,
            providerId = "9876"
        )

        // when
        val findMember1 = memberService.addMemberByOAuth2(member1)
        val findMember2 = memberService.addMemberByOAuth2(member2)

        // then
        assertThat(findMember1)
            .extracting("email", "password", "role", "provider", "providerId")
            .contains(member1.email, member1.password, member1.role, member1.provider, member1.providerId)

        assertThat(findMember2)
            .extracting("email", "password", "role", "provider", "providerId")
            .contains(member2.email, member2.password, member2.role, member2.provider, member2.providerId)
    }


    @DisplayName("회원을 삭제한다.")
    @Test
    fun removeMember() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(member)

        // when
        val deletedMember = memberService.removeMember(member.id!!)

        // then
        assertThat(deletedMember.deletedAt).isNotNull()
    }

    @DisplayName("회원을 삭제하려고 했으나, 조회되는 회원이 없으면 삭제하지 못한다.")
    @Test
    fun removeMemberWithoutMember() {
        // when & then
        assertThatThrownBy {
            memberService.removeMember(1L)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND)
            })
    }

    @DisplayName("없는 회원을 프록시로 가져오면 필드 조회에 실패한다.")
    @Test
    fun findMemberReferenceFromWithoutMember() {
        // when
        val memberProxy = memberRepository.getReferenceById(1L)

        // then
        assertThat(memberProxy).isInstanceOf(HibernateProxy::class.java)
        assertThatThrownBy {
            memberProxy.email
        }
        .isInstanceOf(EntityNotFoundException::class.java)
    }


    @DisplayName("상대 회원의 프로필을 조회한다.")
    @Test
    fun findMemberDetail() {
        // given
        val me = Member(
            email = "email",
            password = "password",
            role = Role.USER
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

        val request = createOAuth2SignupServiceRequest()
        oAuth2Service.signup(request, otherMember.id!!)

        // when
        val response = memberService.findMemberDetail(me.id!!, otherMember.id!!)

        // then
        assertThat(response.profile).isNotNull
        response.profile.let {
            assertThat(it.memberId).isEqualTo(otherMember.id)
            assertThat(it.nickname).isEqualTo(request.nickname)
            assertThat(it.gender).isEqualTo(request.gender)
            assertThat(it.height).isEqualTo(request.height)
            assertThat(it.weight).isEqualTo(request.weight)
            assertThat(it.intro).isEqualTo(request.intro)
            assertThat(it.age).isEqualTo(Age.fromBirthDay(request.birthday))
            assertThat(it.workoutExperience).isEqualTo(request.workoutExperience)
            assertThat(it.workoutStyle).isEqualTo(request.workoutStyle)
            assertThat(it.workoutGoal).isEqualTo(request.workoutGoal)
            assertThat(it.score).isEqualTo(otherMember.score!!.toInt())

            assertThat(it.siDo).isEqualTo(request.address.siDo)
            assertThat(it.siGunGu).isEqualTo(request.address.siGunGu)
            assertThat(it.eupMyeonDong).isEqualTo(request.address.eupMyeonDong)

            assertThat(it.workoutTimeNames).containsExactlyInAnyOrderElementsOf(request.workoutTimes)
            assertThat(it.workoutImageUrls).containsExactlyInAnyOrderElementsOf(request.workoutImageUrls)

            assertThat(it.profileImageUrl).isEqualTo(request.profileImageUrl)
        }
        response.workoutPartner.let {
            assertThat(it.status).isEqualTo(ProfileWorkoutPartnerStatus.NONE)
            assertThat(it.workoutPartnerRequestId).isEqualTo(null)
            assertThat(it.chatRoomId).isEqualTo(null)
        }
    }


    @DisplayName("인근 회원을 페이징 형태로 조회한다.")
    @Test
    fun findRandomMemberWithinLocal() {
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
            siDo = AddressSiDo.BUSAN,
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
            page = 1,
            size = 10
        )

        given(redisClient.get(any()))
            .willReturn(null)

        // when
        val response = memberService.findRandomMemberWithinLocal(
            memberId = baseMember.id!!,
            condition = condition,
            seed = 123L
        )

        // then
        assertThat(response.number).isEqualTo(0)
        assertThat(response.size).isEqualTo(10)
        assertThat(response.numberOfElements).isEqualTo(3)
        assertThat(response.hasNext()).isFalse()


        val content = response.content
        assertThat(content)
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

    @DisplayName("인근 회원 조회 시, seed 기반의 랜덤 셔플링을 해서 조회한다.")
    @Test
    fun findRandomMemberWithinLocalRandom() {
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


        val condition = MemberLocalCondition(
            size = 10
        )

        val seed = 123L

        given(redisClient.get(any()))
            .willReturn(null)

        // when
        val response = memberService.findRandomMemberWithinLocal(
            memberId = baseMember.id!!,
            condition = condition,
            seed = seed
        )

        // then
        val memberMaxId = member3.id!!
        val randomMemberId = randomCustom.nextLong(seed, 1, memberMaxId + 1)
        val members = listOf(member1, member2, member3)

        val left = members.filter { it.id!! >= randomMemberId }.sortedBy { it.id }
        val right = members.filter { it.id!! in 1..<randomMemberId }.sortedBy { it.id }

        val responseMember = left + right

        val shuffledResponseMember = randomCustom.shuffled(seed, responseMember).toMutableList()

        assertThat(response).hasSize(3)
        assertThat(response.content.map { it.memberId })
            .containsExactlyElementsOf(shuffledResponseMember.map { it.id })
    }

    @DisplayName("인근 회원 조회 시, 전체 조회 개수 100개를 미달하면 서울 인원으로 추가 조회한다..")
    @Test
    fun findRandomMemberWithinLocalAddSeoul() {
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
            siDo = AddressSiDo.BUSAN,
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


        val member6 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member6)
        val outAddress3 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 49.90,
            lng = 49.85,
            fullAddress = "fullAddress"
        )
        val signupRequest6 = TestDataFactory.oAuth2SignupRequest(
            address = outAddress3
        )
        oAuth2Service.signup(signupRequest6, member6.id!!)

        val condition = MemberLocalCondition(
            size = 10
        )

        given(redisClient.get(any()))
            .willReturn(null)

        val seed = 123L

        // when
        val response = memberService.findRandomMemberWithinLocal(
            memberId = baseMember.id!!,
            condition = condition,
            seed = seed
        )

        // then
        val memberMaxId = member6.id!!
        val randomMemberId = randomCustom.nextLong(seed, 1, memberMaxId + 1)
        val members = listOf(member1, member2, member3)

        val left = members.filter { it.id!! >= randomMemberId }.sortedBy { it.id }
        val right = members.filter { it.id!! in 1..<randomMemberId }.sortedBy { it.id }

        val responseMember = left + right

        val shuffledResponseMember = randomCustom.shuffled(seed, responseMember).toMutableList()

        val shuffledSeoulMembers = randomCustom.shuffled(seed, listOf(member4, member6))

        shuffledResponseMember.addAll(shuffledSeoulMembers)

        assertThat(response).hasSize(5)
        assertThat(response.content.map { it.memberId })
            .containsExactlyElementsOf(shuffledResponseMember.map { it.id })
    }

    @DisplayName("인근 회원 조회 시, 캐시된 회원 랜덤 id가 있으면 사용한다.")
    @Test
    fun findRandomMemberWithinLocalByCacheMemberRandomId() {
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


        val condition = MemberLocalCondition(
            size = 10
        )

        given(redisClient.get(any()))
            .willReturn(member1.id!!.toString())

        val seed = 123L

        // when
        val response = memberService.findRandomMemberWithinLocal(
            memberId = baseMember.id!!,
            condition = condition,
            seed = seed
        )

        // then
        val members = listOf(member1, member2, member3)
        val shuffledMembers = randomCustom.shuffled(seed, members)

        assertThat(response).hasSize(3)
        assertThat(response.content.map { it.memberId })
            .containsExactlyElementsOf(shuffledMembers.map { it.id })
    }


    @DisplayName("회원 프로필 이미지를 수정한다.")
    @Test
    fun modifyMemberProfileImageUrl() {
        // given
        val member = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member)
        val signupRequest = TestDataFactory.oAuth2SignupRequest()
        oAuth2Service.signup(signupRequest, member.id!!)

        val request = MemberUpdateServiceRequest(
            profileImageUrl = "updateProfile"
        )

        // when
        memberService.modifyMember(member.id!!, request)

        em.flush()
        em.clear()

        // then
        val findMemberImage = memberImageRepository.findWithImageByMemberIdAndProfileAndDeletedAtIsNull(member.id!!)
        val findImage = findMemberImage!!.image
        assertThat(findImage!!.url).isEqualTo("updateProfile")
    }

    @DisplayName("회원 별명을 수정한다.")
    @Test
    fun modifyMemberNickname() {
        // given
        val member = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member)
        val signupRequest = TestDataFactory.oAuth2SignupRequest()
        oAuth2Service.signup(signupRequest, member.id!!)

        val request = MemberUpdateServiceRequest(
            nickname = "updateNickname"
        )

        // when
        memberService.modifyMember(member.id!!, request)

        em.flush()
        em.clear()

        // then
        val findMember = memberRepository.findAll()[0]
        assertThat(findMember.nickname).isEqualTo("updateNickname")
    }


    @DisplayName("회원 자기소개를 수정한다.")
    @Test
    fun modifyMemberIntro() {
        // given
        val member = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member)
        val signupRequest = TestDataFactory.oAuth2SignupRequest()
        oAuth2Service.signup(signupRequest, member.id!!)

        val request = MemberUpdateServiceRequest(
            intro = "updateIntro"
        )

        // when
        memberService.modifyMember(member.id!!, request)

        em.flush()
        em.clear()

        // then
        val findMember = memberRepository.findAll()[0]
        assertThat(findMember.intro).isEqualTo("updateIntro")
    }

    @DisplayName("회원 키를 수정한다.")
    @Test
    fun modifyMemberHeight() {
        // given
        val member = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member)
        val signupRequest = TestDataFactory.oAuth2SignupRequest()
        oAuth2Service.signup(signupRequest, member.id!!)

        val request = MemberUpdateServiceRequest(
            height = 155
        )

        // when
        memberService.modifyMember(member.id!!, request)

        em.flush()
        em.clear()

        // then
        val findMember = memberRepository.findAll()[0]
        assertThat(findMember.height).isEqualTo(155)
    }

    @DisplayName("회원 체중을 수정한다.")
    @Test
    fun modifyMemberWeight() {
        // given
        val member = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member)
        val signupRequest = TestDataFactory.oAuth2SignupRequest()
        oAuth2Service.signup(signupRequest, member.id!!)

        val request = MemberUpdateServiceRequest(
            weight = 99
        )

        // when
        memberService.modifyMember(member.id!!, request)

        em.flush()
        em.clear()

        // then
        val findMember = memberRepository.findAll()[0]
        assertThat(findMember.weight).isEqualTo(99)
    }

    @DisplayName("회원 생일을 수정한다.")
    @Test
    fun modifyMemberBirthday() {
        // given
        val member = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member)
        val signupRequest = TestDataFactory.oAuth2SignupRequest()
        oAuth2Service.signup(signupRequest, member.id!!)

        val request = MemberUpdateServiceRequest(
            birthday = LocalDate.of(1999,12,30)
        )

        // when
        memberService.modifyMember(member.id!!, request)

        em.flush()
        em.clear()

        // then
        val findMember = memberRepository.findAll()[0]
        assertThat(findMember.birthday).isEqualTo(LocalDate.of(1999,12,30))
    }

    @DisplayName("회원 운동 경험을 수정한다.")
    @Test
    fun modifyMemberWorkoutExperience() {
        // given
        val member = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member)
        val signupRequest = TestDataFactory.oAuth2SignupRequest(
            workoutExperience = MemberWorkoutExperience.JUST_STARTED
        )
        oAuth2Service.signup(signupRequest, member.id!!)

        val request = MemberUpdateServiceRequest(
            workoutExperience = MemberWorkoutExperience.FOUR_TO_SIX_YEARS
        )

        // when
        memberService.modifyMember(member.id!!, request)

        em.flush()
        em.clear()

        // then
        val findMember = memberRepository.findAll()[0]
        assertThat(findMember.workoutExperience).isEqualTo(MemberWorkoutExperience.FOUR_TO_SIX_YEARS)
    }

    @DisplayName("회원 운동 스타일을 수정한다.")
    @Test
    fun modifyMemberWorkoutStyle() {
        // given
        val member = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member)
        val signupRequest = TestDataFactory.oAuth2SignupRequest(
            workoutStyle = MemberWorkoutStyle.PARTNER
        )
        oAuth2Service.signup(signupRequest, member.id!!)

        val request = MemberUpdateServiceRequest(
            workoutStyle = MemberWorkoutStyle.PERFORMANCE
        )

        // when
        memberService.modifyMember(member.id!!, request)

        em.flush()
        em.clear()

        // then
        val findMember = memberRepository.findAll()[0]
        assertThat(findMember.workoutStyle).isEqualTo(MemberWorkoutStyle.PERFORMANCE)
    }

    @DisplayName("회원 운동 목표를 수정한다.")
    @Test
    fun modifyMemberWorkoutGoal() {
        // given
        val member = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member)
        val signupRequest = TestDataFactory.oAuth2SignupRequest(
            workoutGoal = MemberWorkoutGoal.BODY_CORRECTION
        )
        oAuth2Service.signup(signupRequest, member.id!!)

        val request = MemberUpdateServiceRequest(
            workoutGoal = MemberWorkoutGoal.HEALTH_MAINTENANCE
        )

        // when
        memberService.modifyMember(member.id!!, request)

        em.flush()
        em.clear()

        // then
        val findMember = memberRepository.findAll()[0]
        assertThat(findMember.workoutGoal).isEqualTo(MemberWorkoutGoal.HEALTH_MAINTENANCE)
    }

    @DisplayName("회원 운동 시간을 수정한다.")
    @Test
    fun modifyMemberWorkoutTimes() {
        // given
        val member = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member)
        val signupRequest = TestDataFactory.oAuth2SignupRequest(
            workoutTimes = listOf(
                WorkoutTimeName.WEEKDAY_DAWN,
                WorkoutTimeName.WEEKDAY_EVENING
            ),
        )
        oAuth2Service.signup(signupRequest, member.id!!)

        val request = MemberUpdateServiceRequest(
            workoutTimes = listOf(
                WorkoutTimeName.WEEKEND_AFTERNOON,
                WorkoutTimeName.WEEKEND_MORNING
            ),
        )

        // when
        memberService.modifyMember(member.id!!, request)

        em.flush()
        em.clear()

        // then
        val findMember = memberRepository.findById(member.id!!).orElseThrow()
        val findWorkoutTimes = workoutTimeRepository.findAllByMemberIdAndDeletedAtIsNull(member.id!!)

        assertThat(findWorkoutTimes).hasSize(2)
        assertThat(findWorkoutTimes)
            .extracting("member", "name")
            .containsExactlyInAnyOrder(
                tuple(findMember, WorkoutTimeName.WEEKEND_AFTERNOON),
                tuple(findMember, WorkoutTimeName.WEEKEND_MORNING)
            )
    }

    @DisplayName("회원 운동 사진을 수정한다.")
    @Test
    fun modifyMemberWorkoutImageUrls() {
        // given
        val member = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member)
        val signupRequest = TestDataFactory.oAuth2SignupRequest(
            workoutImageUrls = listOf(
                "workoutImageUrl1",
                "workoutImageUrl2",
            )
        )
        oAuth2Service.signup(signupRequest, member.id!!)

        val request = MemberUpdateServiceRequest(
            workoutImageUrls = listOf(
                "updateWorkoutImageUrl1",
                "updateWorkoutImageUrl2",
            )
        )

        // when
        memberService.modifyMember(member.id!!, request)

        em.flush()
        em.clear()

        // then
        val findWorkoutImages = memberImageRepository.findWithImageByMemberIdAndWorkoutAndDeletedAtIsNull(member.id!!)
        val findImages = findWorkoutImages.map{it.image}

        assertThat(findImages)
            .extracting("url")
            .containsExactlyInAnyOrder(
                "updateWorkoutImageUrl1",
                "updateWorkoutImageUrl2"
            )
    }

    @DisplayName("추천 핏버디 - 운동 경력/스타일/목표가 2개 이상 맞는 회원을 조회한다.")
    @Test
    fun findRandomMemberWithinRecommendation() {
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
        val address1 = AddressCreateServiceRequest(
            siDo = AddressSiDo.BUSAN,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 37.4979,
            lng = 127.0276,
            fullAddress = "fullAddress"
        )
        memberRepository.save(notMatchMember1)
        val signupRequest5 = TestDataFactory.oAuth2SignupRequest(
            workoutExperience = MemberWorkoutExperience.JUST_STARTED,
            workoutStyle = MemberWorkoutStyle.PERFORMANCE,
            workoutGoal = MemberWorkoutGoal.STRENGTH_GAIN,
            address = address1
        )
        oAuth2Service.signup(signupRequest5, notMatchMember1.id!!)

        val seed = 123L

        // when
        val response = memberService.findRandomMemberWithinRecommendation(
            memberId = me.id!!,
            size = 10,
            seed = seed
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

    @DisplayName("추천 핏버디 조회 시, 조회 개수가 못미치면 아무 서울 인원도 추가 조회한다.")
    @Test
    fun findRandomMemberWithinRecommendationNotEnoughMember() {
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
        val address1 = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "siGunGu",
            eupMyeonDong = "eupMyeonDong",
            lat = 37.4979,
            lng = 127.0276,
            fullAddress = "fullAddress"
        )
        memberRepository.save(notMatchMember1)
        val signupRequest5 = TestDataFactory.oAuth2SignupRequest(
            nickname = "update4",
            workoutExperience = MemberWorkoutExperience.FOUR_TO_SIX_YEARS,
            workoutStyle = MemberWorkoutStyle.PARTNER,
            workoutGoal = MemberWorkoutGoal.STRENGTH_GAIN,
            address = address1,
            workoutImageUrls = listOf("updateMember4Workout1", "updateMember4Workout2")
        )
        oAuth2Service.signup(signupRequest5, notMatchMember1.id!!)

        val seed = 123L

        // when
        val response = memberService.findRandomMemberWithinRecommendation(
            memberId = me.id!!,
            size = 10,
            seed = seed
        )

        // then
        assertThat(response).hasSize(4)
        assertThat(response)
            .extracting("memberId", "nickname", "workoutImageUrl")
            .containsExactlyInAnyOrder(
                tuple(matchMember1.id!!, "update1", "updateMember1Workout1"),
                tuple(matchMember2.id!!, "update2", "updateMember2Workout1"),
                tuple(matchMember3.id!!, "update3", "updateMember3Workout1"),
                tuple(notMatchMember1.id!!, "update4", "updateMember4Workout1"),
            )
    }

}