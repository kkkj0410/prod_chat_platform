package kr.co.fitview.api.app.domain.workout_partner.entity

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.member.service.MemberService
import kr.co.fitview.api.app.domain.workout_partner.entity.enums.WorkoutPartnerRequestStatus
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.workout_partner.WorkoutPartnerErrorCode
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class WorkoutPartnerTest @Autowired constructor(
    private val memberRepository : MemberRepository
) : IntegrationTestSupport(){

    @DisplayName("운동 요청을 만든다.")
    @Test
    fun of() {
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

        // when
        val workoutPartner = WorkoutPartner.of(member1, member2)

        // then
        assertThat(workoutPartner)
            .extracting("memberOne", "memberTwo")
            .contains(member1, member2)
    }

    @DisplayName("운동 요청의 회원을 순서대로 정렬해서 만든다.")
    @Test
    fun ofOrdered() {
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

        // when
        val workoutPartner = WorkoutPartner.of(member2, member1)

        // then
        assertThat(workoutPartner)
            .extracting("memberOne", "memberTwo")
            .contains(member1, member2)
    }

    @DisplayName("id가 없는 회원으로는 운동 요청을 만들지 않는다.")
    @Test
    fun ofNotMemberId() {
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

        // when & then
        assertThatThrownBy {
            WorkoutPartner.of(member1, member2)
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("WorkoutPartner.of() requires both members to have non-null IDs")
    }
}