package kr.co.fitview.api.app.domain.member.repository

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.entity.WorkoutTime
import kr.co.fitview.api.app.domain.member.entity.enums.WorkoutTimeName
import kr.co.fitview.api.app.domain.term.entity.enums.TermName
import kr.co.fitview.api.app.global.entity.Role
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class WorkoutTimeRepositoryTest @Autowired constructor(
    val workoutTimeRepository: WorkoutTimeRepository,
    val memberRepository: MemberRepository
) : IntegrationTestSupport() {

    @DisplayName("해당 회원의 선호시간을 모두 찾는다.")
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
            WorkoutTime(
                savedMember,
                WorkoutTimeName.WEEKDAY_DAWN
            ),
            WorkoutTime(
                savedMember,
                WorkoutTimeName.WEEKEND_EVENING
            )
        )
        workoutTimeRepository.saveAll(workoutDays)

        // when
        val findWorkoutTimes = workoutTimeRepository.findAllByMemberIdAndDeletedAtIsNull(savedMember.id!!)

        // then
        assertThat(workoutDays)
            .allSatisfy { workoutDay ->
                assertThat(workoutDay.id).isNotNull()
            }

        assertThat(findWorkoutTimes)
            .extracting("member", "name")
            .containsExactlyInAnyOrder(
                tuple(savedMember, WorkoutTimeName.WEEKDAY_DAWN),
                tuple(savedMember, WorkoutTimeName.WEEKEND_EVENING),
            )
    }

    @DisplayName("해당 회원의 선호시간이 없으면 선호시간을 찾지 못한다.")
    @Test
    fun findAllByMemberIdAndDeletedAtIsNullWithoutWorkoutTime() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val savedMember = memberRepository.save(member)

        // when
        val findWorkoutTimes = workoutTimeRepository.findAllByMemberIdAndDeletedAtIsNull(savedMember.id!!)

        // then
        assertThat(findWorkoutTimes).isEmpty()
    }
}