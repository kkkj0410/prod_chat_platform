package kr.co.fitview.api.app.domain.workout.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.workout.entity.WorkoutHistory
import kr.co.fitview.api.app.domain.workout.repository.WorkoutHistoryRepository
import kr.co.fitview.api.app.global.entity.Role
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class WorkoutHistoryServiceTest @Autowired constructor(
    val workoutHistoryRepository : WorkoutHistoryRepository,
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

    @DisplayName("회원 A, B간의 운동 완료 이력이 있다.")
    @Test
    fun existsWorkoutHistoryFrom() {
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
        val existsWorkoutHistory = workoutHistoryService.existsWorkoutHistoryFrom(member1.id!!, member2.id!!)

        // then
        assertThat(existsWorkoutHistory).isEqualTo(true)
    }

    @DisplayName("회원 A, B간의 운동 완료 이력이 없다.")
    @Test
    fun existsWorkoutHistoryFromNotExists() {
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
        val existsWorkoutHistory = workoutHistoryService.existsWorkoutHistoryFrom(member1.id!!, member2.id!!)

        // then
        assertThat(existsWorkoutHistory).isEqualTo(false)
    }

    @DisplayName("회원 A, B간의 운동 완료 이력 확인 시, 회원 순서가 뒤바뀌어도 찾는다.")
    @Test
    fun existsWorkoutHistoryFromReverse() {
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
        val existsWorkoutHistory = workoutHistoryService.existsWorkoutHistoryFrom(member2.id!!, member1.id!!)

        // then
        assertThat(existsWorkoutHistory).isEqualTo(true)
    }

}