package kr.co.fitview.api.app.domain.workout_partner.entity

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class WorkoutPartnerTest @Autowired constructor(
    val time : Time
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

        val workoutPartner = WorkoutPartner(
            fromMember,
            toMember,
            time.nowLocalDateTime
        )

        // when
        workoutPartner.reject(time.nowLocalDateTime)

        // then
        assertThat(workoutPartner)
            .extracting("fromMember", "toMember", "requestedAt", "rejectedAt")
            .contains(fromMember, toMember, time.nowLocalDateTime, time.nowLocalDateTime)
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

        val workoutPartner = WorkoutPartner(
            fromMember,
            toMember,
            time.nowLocalDateTime
        )

        // when
        workoutPartner.accept(time.nowLocalDateTime)

        // then
        assertThat(workoutPartner)
            .extracting("fromMember", "toMember", "requestedAt", "acceptedAt")
            .contains(fromMember, toMember, time.nowLocalDateTime, time.nowLocalDateTime)
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

        val workoutPartner = WorkoutPartner(
            fromMember,
            toMember,
            time.nowLocalDateTime
        )

        // when
        workoutPartner.cancel(time.nowLocalDateTime)

        // then
        assertThat(workoutPartner)
            .extracting("fromMember", "toMember", "requestedAt", "canceledAt")
            .contains(fromMember, toMember, time.nowLocalDateTime, time.nowLocalDateTime)
    }
}