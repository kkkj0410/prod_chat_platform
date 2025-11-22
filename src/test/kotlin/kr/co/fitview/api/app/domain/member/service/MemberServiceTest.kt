package kr.co.fitview.api.app.domain.member.service

import jakarta.persistence.EntityManager
import jakarta.persistence.EntityNotFoundException
import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.address.dto.request.AddressCreateServiceRequest
import kr.co.fitview.api.app.domain.address.entity.enums.AddressSiDo
import kr.co.fitview.api.app.domain.member.dto.request.Age
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
import kr.co.fitview.api.app.domain.workout_partner.dto.request.enums.WorkoutPartnerRequestType
import kr.co.fitview.api.app.global.entity.Gender
import kr.co.fitview.api.app.global.entity.OAuth2Provider
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.global.GlobalErrorCode
import kr.co.fitview.api.app.global.exception.error.member.MemberErrorCode
import org.assertj.core.api.Assertions.*
import org.assertj.core.api.ThrowingConsumer
import org.hibernate.proxy.HibernateProxy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate

class MemberServiceTest @Autowired constructor(
    val memberService : MemberService,
    val memberRepository : MemberRepository,
    val workoutTimeRepository : WorkoutTimeRepository,
    val oAuth2Service : OAuth2Service,
    val em : EntityManager
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
        intro: String? = "intro",
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

    @DisplayName("회원이 선호 운동시간을 기록하는데, 이미 DB에 있으면 저장하지 않는다.")
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
                tuple(savedMember, WorkoutTimeName.WEEKDAY_EVENING),
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
                tuple(savedMember, WorkoutTimeName.WEEKDAY_EVENING),
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

    @DisplayName("회원을 프록시로 가져온다.")
    @Test
    fun findMemberReferenceFrom() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val savedMember = memberRepository.save(member)

        em.flush()
        em.clear()

        // when
        val memberProxy = memberRepository.getReferenceById(savedMember.id!!)

        // then
        assertThat(memberProxy).isInstanceOf(HibernateProxy::class.java)
        assertThat(memberProxy::class.simpleName!!).contains("Member")
        assertThat(memberProxy)
            .extracting("email", "password", "role")
            .contains(member.email, member.password, member.role)
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

    @DisplayName("회원의 채팅방 프로필을 확인한다.")
    @Test
    fun findMemberChatProfileFrom() {
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
        val response = memberService.findMemberChatProfileFrom(me.id!!)

        // then
        assertThat(response)
            .extracting("profileImageUrl", "nickname")
            .contains(request.profileImageUrl, request.nickname)
    }

}