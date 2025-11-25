package kr.co.fitview.api.app.domain.workout.entity

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.workout.repository.WorkoutHistoryRepository
import kr.co.fitview.api.app.global.entity.Role
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class WorkoutHistoryTest @Autowired constructor(
    val memberRepository : MemberRepository
) : IntegrationTestSupport(){

    @DisplayName("회원 순서는 회원 id의 오름차순으로 정렬해서 운동 기록에 담는다.")
    @Test
    fun of() {
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
        val workoutHistory = WorkoutHistory.of(
            memberOne = member2,
            memberTwo = member1
        )

        // then
        assertThat(workoutHistory)
            .extracting("memberOne", "memberTwo")
            .contains(member1, member2)
    }
}