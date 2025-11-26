package kr.co.fitview.api.app.domain.workout.repository

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.workout.entity.WorkoutHistory
import kr.co.fitview.api.app.global.entity.Role
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class WorkoutHistoryRepositoryTest @Autowired constructor(
    val workoutHistoryRepository: WorkoutHistoryRepository,
    val memberRepository : MemberRepository,

) : IntegrationTestSupport(){

    @DisplayName("회원 간의 운동 이력 여부가 있다.")
    @Test
    fun existsByMemberOneIdAndMemberTwoIdAndDeletedAtIsNull() {
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
        val existsWorkoutHistory = workoutHistoryRepository.existsByMemberOneIdAndMemberTwoIdAndDeletedAtIsNull(member1.id!!, member2.id!!)

        // then
        assertThat(existsWorkoutHistory).isEqualTo(true)
    }

    @DisplayName("회원 간의 운동 이력 여부가 없다")
    @Test
    fun existsByMemberOneIdAndMemberTwoIdAndDeletedAtIsNullIsFalse() {
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
        val existsWorkoutHistory = workoutHistoryRepository.existsByMemberOneIdAndMemberTwoIdAndDeletedAtIsNull(member1.id!!, member2.id!!)

        // then
        assertThat(existsWorkoutHistory).isEqualTo(false)
    }

    @DisplayName("회원 간의 운동 이력 여부 확인 시, 회원 순서가 뒤바뀌면 여부 확인이 안된다.")
    @Test
    fun existsByMemberOneIdAndMemberTwoIdAndDeletedAtIsNullReverse() {
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
        val existsWorkoutHistory = workoutHistoryRepository.existsByMemberOneIdAndMemberTwoIdAndDeletedAtIsNull(member2.id!!, member1.id!!)

        // then
        assertThat(existsWorkoutHistory).isEqualTo(false)
    }

    @DisplayName("회원 간의 운동 여부 확인 시, 회원의 순서를 오름차순으로 자동 정렬해서 조회한다.")
    @Test
    fun findByOrderedMemberOneIdAndMemberTwoIdAndDeletedAtIsNull() {
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
        val existsWorkoutHistory = workoutHistoryRepository.findByOrderedMemberOneIdAndMemberTwoIdAndDeletedAtIsNull(member2.id!!, member1.id!!)

        // then
        assertThat(existsWorkoutHistory).isEqualTo(true)

    }
}