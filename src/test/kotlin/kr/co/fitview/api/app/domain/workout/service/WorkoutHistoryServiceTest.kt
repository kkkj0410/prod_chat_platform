package kr.co.fitview.api.app.domain.workout.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.global.entity.Role
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class WorkoutHistoryServiceTest @Autowired constructor(
    val workoutHistoryService: WorkoutHistoryService,
    val memberRepository : MemberRepository
) : IntegrationTestSupport(){

    @DisplayName("성공한 운동 요청 완료를 기록한다.")
    @Test
    fun addWorkoutHistory() {
        // given
        val me = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        val other = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(me)
        memberRepository.save(other)

        // when
        val savedWorkoutHistory = workoutHistoryService.addWorkoutHistory(me, other)

        // then
        assertThat(savedWorkoutHistory.id).isNotNull()
        assertThat(savedWorkoutHistory)
            .extracting("memberOne", "memberTwo")
            .contains(me, other)
    }

    @DisplayName("성공한 운동 완료 요청을 기록하되, 회원 순서가 뒤바뀌어도 오름차순으로 기록한다.")
    @Test
    fun addWorkoutHistoryOrdered() {
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

        // when
        val savedWorkoutHistory = workoutHistoryService.addWorkoutHistory(member2, member1)

        // then
        assertThat(savedWorkoutHistory.id).isNotNull()
        assertThat(savedWorkoutHistory)
            .extracting("memberOne", "memberTwo")
            .contains(member1, member2)

    }
}