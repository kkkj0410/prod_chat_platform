package kr.co.fitview.api.app.domain.workout_partner.entity

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.workout_partner.entity.enums.WorkoutPartnerRequestContent
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

    @DisplayName("파트너 요청을 만든다")
    @Test
    fun of() {
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


        // when
        val workoutPartnerRequest = WorkoutPartnerRequest.of(
            fromMember = fromMember,
            toMember = toMember,
            now = time.nowLocalDateTime,
            content = WorkoutPartnerRequestContent.BURN
        )

        // then
        assertThat(workoutPartnerRequest)
            .extracting("fromMember", "toMember", "requestedAt", "status")
            .contains(fromMember, toMember, time.nowLocalDateTime, WorkoutPartnerRequestStatus.PENDING)
    }

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
            now = time.nowLocalDateTime,
            content = WorkoutPartnerRequestContent.BURN
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
            now = time.nowLocalDateTime,
            content = WorkoutPartnerRequestContent.BURN
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
            now = time.nowLocalDateTime,
            content = WorkoutPartnerRequestContent.BURN
        )

        // when
        workoutPartnerRequest.cancel()

        // then
        assertThat(workoutPartnerRequest)
            .extracting("fromMember", "toMember", "requestedAt", "status")
            .contains(fromMember, toMember, time.nowLocalDateTime, WorkoutPartnerRequestStatus.CANCEL)
    }


    @DisplayName("요청 회원의 id를 가져온다.")
    @Test
    fun getFromMemberId() {
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
            now = time.nowLocalDateTime,
            content = WorkoutPartnerRequestContent.BURN
        )

        // when
        val fromMemberId = workoutPartnerRequest.getFromMemberId()

        // then
        assertThat(fromMemberId).isEqualTo(fromMember.id!!)
    }

    @DisplayName("")
    @Test
    fun getToMemberId() {
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
            now = time.nowLocalDateTime,
            content = WorkoutPartnerRequestContent.BURN
        )

        // when
        val toMemberId = workoutPartnerRequest.getToMemberId()

        // then
        assertThat(toMemberId).isEqualTo(toMember.id!!)
    }

}