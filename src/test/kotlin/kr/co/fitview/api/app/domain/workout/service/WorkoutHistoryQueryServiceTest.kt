package kr.co.fitview.api.app.domain.workout.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.workout.entity.WorkoutHistory
import kr.co.fitview.api.app.domain.workout.repository.WorkoutHistoryRepository
import kr.co.fitview.api.app.global.entity.Role
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class WorkoutHistoryQueryServiceTest @Autowired constructor(
    val workoutHistoryRepository: WorkoutHistoryRepository,
    val memberRepository : MemberRepository,
    val workoutHistoryQueryService : WorkoutHistoryQueryService
) : IntegrationTestSupport(){


    @DisplayName("운동 이력 조회")
    @Test
    fun findWorkoutHistoryFrom() {
        // given
        val member1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        val member2 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member1)
        memberRepository.save(member2)

        val workoutHistory = WorkoutHistory.of(
            memberOne = member1,
            memberTwo = member2
        )
        workoutHistoryRepository.save(workoutHistory)

        // when
        val findWorkoutHistory = workoutHistoryQueryService.findWorkoutHistoryFrom(workoutHistory.id!!)

        // then
        assertThat(findWorkoutHistory)
            .extracting("memberOne", "memberTwo")
            .contains(member1, member2)
    }
}