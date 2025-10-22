package kr.co.fitview.api.app.domain.member.repository

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.entity.WorkoutDay
import kr.co.fitview.api.app.domain.member.entity.enums.WorkoutDayName
import kr.co.fitview.api.app.domain.term.entity.enums.TermName
import kr.co.fitview.api.app.global.entity.Role
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class WorkoutDayRepositoryTest @Autowired constructor(
    val workoutDayRepository: WorkoutDayRepository,
    val memberRepository: MemberRepository
) : IntegrationTestSupport() {

    @DisplayName("해당 회원의 선호요일을 모두 찾는다.")
    @Test
    fun findAllByMemberIdAndDeletedAtIsNull() {
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

        // when
        val findWorkoutDays = workoutDayRepository.findAllByMemberIdAndDeletedAtIsNull(savedMember.id!!)

        // then
        assertThat(workoutDays)
            .allSatisfy { workoutDay ->
                assertThat(workoutDay.id).isNotNull()
            }

        assertThat(findWorkoutDays)
            .extracting("member", "name")
            .containsExactlyInAnyOrder(
                tuple(savedMember, WorkoutDayName.FRI),
                tuple(savedMember, WorkoutDayName.SUN),
            )
    }

    @DisplayName("해당 회원의 선호요일이 없으면 선호요일을 찾지 못한다.")
    @Test
    fun findAllByMemberIdAndDeletedAtIsNullWithoutWorkoutDay() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val savedMember = memberRepository.save(member)

        // when
        val findWorkoutDays = workoutDayRepository.findAllByMemberIdAndDeletedAtIsNull(savedMember.id!!)

        // then
        assertThat(findWorkoutDays).isEmpty()
    }
}