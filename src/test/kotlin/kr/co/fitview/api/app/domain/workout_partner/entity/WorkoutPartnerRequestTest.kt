package kr.co.fitview.api.app.domain.workout_partner.entity

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.workout_partner.entity.enums.WorkoutPartnerRequestStatus
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class WorkoutPartnerRequestTest @Autowired constructor(
    val time : Time,
    val memberRepository : MemberRepository
) : IntegrationTestSupport(){


    @DisplayName("핏버디 요청을 거부한다.")
    @Test
    fun reject() {
        // given
        val fromMember = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        val toMember = Member(
            email = "email2",
            password = "password2",
            role = Role.USER,
        )
        memberRepository.save(fromMember)
        memberRepository.save(toMember)

        val workoutPartnerRequest = WorkoutPartnerRequest.of(
            fromMember = fromMember,
            toMember = toMember,
            now = time.nowLocalDateTime
        )

        // when
        workoutPartnerRequest.reject()

        // then
        assertThat(workoutPartnerRequest)
            .extracting("fromMember", "toMember", "requestedAt", "status")
            .contains(fromMember, toMember, time.nowLocalDateTime, WorkoutPartnerRequestStatus.REJECT)
    }

    @DisplayName("핏버디 요청을 수락한다.")
    @Test
    fun accept() {
        // given
        val fromMember = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        val toMember = Member(
            email = "email2",
            password = "password2",
            role = Role.USER,
        )
        memberRepository.save(fromMember)
        memberRepository.save(toMember)

        val workoutPartnerRequest = WorkoutPartnerRequest.of(
            fromMember = fromMember,
            toMember = toMember,
            now = time.nowLocalDateTime
        )

        // when
        workoutPartnerRequest.accept()

        // then
        assertThat(workoutPartnerRequest)
            .extracting("fromMember", "toMember", "requestedAt", "status")
            .contains(fromMember, toMember, time.nowLocalDateTime, WorkoutPartnerRequestStatus.ACCEPT)
    }

    @DisplayName("핏버디 요청을 본인 스스로 취소한다.")
    @Test
    fun cancel() {
        // given
        val fromMember = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        val toMember = Member(
            email = "email2",
            password = "password2",
            role = Role.USER,
        )
        memberRepository.save(fromMember)
        memberRepository.save(toMember)

        val workoutPartnerRequest = WorkoutPartnerRequest.of(
            fromMember = fromMember,
            toMember = toMember,
            now = time.nowLocalDateTime
        )

        // when
        workoutPartnerRequest.cancel()

        // then
        assertThat(workoutPartnerRequest)
            .extracting("fromMember", "toMember", "requestedAt", "status")
            .contains(fromMember, toMember, time.nowLocalDateTime, WorkoutPartnerRequestStatus.CANCEL)
    }

    @DisplayName("")
    @Test
    fun getFromMemberId() {
        // given

        // when

        // then

    }

    @DisplayName("")
    @Test
    fun getToMemberId() {
        // given

        // when

        // then

    }

    @DisplayName("")
    @Test
    fun of() {
        // given

        // when

        // then

    }
}