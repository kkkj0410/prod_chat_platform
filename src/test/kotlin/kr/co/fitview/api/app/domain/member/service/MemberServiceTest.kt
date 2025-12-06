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

}