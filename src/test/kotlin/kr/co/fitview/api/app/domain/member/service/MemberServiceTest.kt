package kr.co.fitview.api.app.domain.member.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.entity.WorkoutDay
import kr.co.fitview.api.app.domain.member.entity.WorkoutTime
import kr.co.fitview.api.app.domain.member.entity.enums.WorkoutDayName
import kr.co.fitview.api.app.domain.member.entity.enums.WorkoutTimeName
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.member.repository.WorkoutDayRepository
import kr.co.fitview.api.app.domain.member.repository.WorkoutTimeRepository
import kr.co.fitview.api.app.global.entity.OAuth2Provider
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.member.MemberErrorCode
import org.assertj.core.api.Assertions.*
import org.assertj.core.api.ThrowingConsumer
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class MemberServiceTest @Autowired constructor(
    val memberService : MemberService,
    val memberRepository : MemberRepository,
    val workoutDayRepository: WorkoutDayRepository,
    val workoutTimeRepository : WorkoutTimeRepository
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
   fun findMemberFromLoginId() {
       // given
       val member = Member(
           email = "email",
           password = "password",
           role = Role.USER,
       )
       memberRepository.save(member)

       // when
       val findMember = memberService.findMemberFromLoginId(member.email!!)

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
        val findMember = memberService.findMemberFromLoginId(email)

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
        val findMember = memberService.findMemberFromLoginId(member.id!!)

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
        val findMember = memberService.findMemberFromLoginId(memberId)

        // then
        assertThat(findMember).isNull()
    }


    @DisplayName("회원 id로 회원 정보를 조회한다.")
    @Test
    fun findMemberMe() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val savedMember = memberRepository.save(member)

        // when
        val response = memberService.findMemberMe(savedMember.id!!)

        // then
        assertThat(response.email).isEqualTo(savedMember.email)
        assertThat(response.role).isEqualTo(savedMember.role)
    }

    @DisplayName("회원 정보가 없으면 회원 고유 id로 사용자 정보를 조회할 수 없다.")
    @Test
    fun findMemberMeWithoutMember() {
        // given
        val memberId = 100L

        // when & then
        assertThatThrownBy {
            memberService.findMemberMe(memberId)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND)
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

    @DisplayName("회원은 선호 운동요일을 기록하고, 회원의 전체 선호운동요일을 반환한다.")
    @Test
    fun addWorkoutDays() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val savedMember = memberRepository.save(member)

        val workoutDayNames = listOf(
                WorkoutDayName.FRI,
                WorkoutDayName.SUN
        )

        // when
        val workoutDays = memberService.addWorkoutDays(savedMember, workoutDayNames)

        // then
        assertThat(workoutDays)
            .allSatisfy { workoutDay ->
                assertThat(workoutDay.id).isNotNull()
            }

        assertThat(workoutDays)
            .extracting("member", "name")
            .containsExactlyInAnyOrder(
                tuple(savedMember, WorkoutDayName.FRI),
                tuple(savedMember, WorkoutDayName.SUN),
            )

    }

    @DisplayName("회원이 선호 운동요일을 기록하는데, 이미 DB에 있으면 저장하지 않는다.")
    @Test
    fun addWorkoutDaysDuplicatedWorkoutDay() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val savedMember = memberRepository.save(member)

        val workoutDays = listOf(
            WorkoutDay(
                savedMember,
                WorkoutDayName.FRI
            ),
            WorkoutDay(
                savedMember,
                WorkoutDayName.SUN
            )
        )
        workoutDayRepository.saveAll(workoutDays)

        val workoutDayNames = listOf(
            WorkoutDayName.FRI,
            WorkoutDayName.MON
        )

        // when
        val returnWorkoutDays = memberService.addWorkoutDays(savedMember, workoutDayNames)

        // then
        assertThat(returnWorkoutDays)
            .allSatisfy { workoutDay ->
                assertThat(workoutDay.id).isNotNull()
            }

        assertThat(returnWorkoutDays)
            .extracting("member", "name")
            .containsExactlyInAnyOrder(
                tuple(savedMember, WorkoutDayName.FRI),
                tuple(savedMember, WorkoutDayName.SUN),
                tuple(savedMember, WorkoutDayName.MON),
            )

        assertThat(returnWorkoutDays)
            .extracting("name")
            .doesNotHaveDuplicates()

        val findWorkoutDays = workoutDayRepository.findAllByMemberIdAndDeletedAtIsNull(savedMember.id!!)

        assertThat(findWorkoutDays)
            .allSatisfy { workoutDay ->
                assertThat(workoutDay.id).isNotNull()
            }

        assertThat(findWorkoutDays)
            .extracting("member", "name")
            .containsExactlyInAnyOrder(
                tuple(savedMember, WorkoutDayName.FRI),
                tuple(savedMember, WorkoutDayName.SUN),
                tuple(savedMember, WorkoutDayName.MON),
            )

        assertThat(findWorkoutDays)
            .extracting("name")
            .doesNotHaveDuplicates()
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

}