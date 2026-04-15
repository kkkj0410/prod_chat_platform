package kr.co.fitview.api.app.domain.workout_partner.repository

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartner
import kr.co.fitview.api.app.global.entity.Role
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate

class WorkoutPartnerRepositoryTest @Autowired constructor(
    val workoutPartnerRepository : WorkoutPartnerRepository,
    val memberRepository : MemberRepository
) : IntegrationTestSupport() {

    @DisplayName("회원 간의 운동 파트너 여부를 확인한다.")
    @Test
    fun findByMemberOneIdAndMemberTwoIdAndDeletedAtIsNull() {
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
        memberRepository.save(member1)
        memberRepository.save(member2)

        val workoutPartner = WorkoutPartner.of(member1, member2)
        workoutPartnerRepository.save(workoutPartner)

        // when
        val findWorkoutPartner = workoutPartnerRepository.findByMemberOneIdAndMemberTwoIdAndDeletedAtIsNull(member1.id!!, member2.id!!)

        // then
        assertThat(findWorkoutPartner!!.id).isNotNull()
        assertThat(findWorkoutPartner)
            .extracting("memberOne", "memberTwo")
            .contains(member1, member2)
    }

    @DisplayName("회원 간의 운동 파트너 여부를 찾되, 회원 순서가 뒤바뀌면 찾지 못한다.")
    @Test
    fun findByMemberOneIdAndMemberTwoIdAndDeletedAtIsNullReverse() {
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
        memberRepository.save(member1)
        memberRepository.save(member2)

        val workoutPartner = WorkoutPartner.of(member1, member2)
        workoutPartnerRepository.save(workoutPartner)

        // when
        val findWorkoutPartner = workoutPartnerRepository.findByMemberOneIdAndMemberTwoIdAndDeletedAtIsNull(member2.id!!, member1.id!!)

        // then
        assertThat(findWorkoutPartner).isNull()
    }

    @DisplayName("회원 간의 운동 파트너 여부를 확인한다.")
    @Test
    fun findByOrderedMemberOneIdAndMemberTwoIdAndDeletedAtIsNull() {
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
        memberRepository.save(member1)
        memberRepository.save(member2)

        val workoutPartner = WorkoutPartner.of(member1, member2)
        workoutPartnerRepository.save(workoutPartner)

        // when
        val findWorkoutPartner = workoutPartnerRepository.findByOrderedMemberOneIdAndMemberTwoIdAndDeletedAtIsNull(member1.id!!, member2.id!!)

        // then
        assertThat(findWorkoutPartner!!.id).isNotNull()
        assertThat(findWorkoutPartner)
            .extracting("memberOne", "memberTwo")
            .contains(member1, member2)
    }

    @DisplayName("회원 간의 운동 파트너 여부를 찾되, 회원 순서가 뒤바뀌어도 찾는다.")
    @Test
    fun findByOrderedMemberOneIdAndMemberTwoIdAndDeletedAtIsNullReverse() {
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
        memberRepository.save(member1)
        memberRepository.save(member2)

        val workoutPartner = WorkoutPartner.of(member1, member2)
        workoutPartnerRepository.save(workoutPartner)

        // when
        val findWorkoutPartner = workoutPartnerRepository.findByOrderedMemberOneIdAndMemberTwoIdAndDeletedAtIsNull(member2.id!!, member1.id!!)

        // then
        assertThat(findWorkoutPartner!!.id).isNotNull()
        assertThat(findWorkoutPartner)
            .extracting("memberOne", "memberTwo")
            .contains(member1, member2)
    }

    @DisplayName("특정 범위 운동 파트너 개수를 조회한다.")
    @Test
    fun countByCreatedAtBetweenAndDeletedAtIsNull() {
        // given
        val targetDate = LocalDate.of(2050, 5, 25)
        val startTime = targetDate.atStartOfDay()
        val endTime = targetDate.atTime(23, 59, 59)
        val beforeTarget = startTime.minusSeconds(1)

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

        val workoutPartner1 = WorkoutPartner.of(
            memberOne = member1,
            memberTwo = member2
        )
        workoutPartner1.createdAt = startTime
        workoutPartnerRepository.save(workoutPartner1)

        val workoutPartner2 = WorkoutPartner.of(
            memberOne = member1,
            memberTwo = member3
        )
        workoutPartner2.createdAt = endTime
        workoutPartnerRepository.save(workoutPartner2)

        val workoutPartner3 = WorkoutPartner.of(
            memberOne = member2,
            memberTwo = member3
        )
        workoutPartner3.createdAt = beforeTarget
        workoutPartnerRepository.save(workoutPartner3)

        // when
        val response = workoutPartnerRepository.countByCreatedAtBetweenAndDeletedAtIsNull(
            start = startTime,
            end = endTime
        )

        // then
        assertThat(response).isEqualTo(2)
    }
}