package kr.co.fitview.api.app.domain.member.entity

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.global.entity.Role
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class MemberTest : IntegrationTestSupport(){


    @DisplayName("회원 평점을 올린다.")
    @Test
    fun updateScore() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )

        // when
        member.updateScore(1.0)

        // then
        assertThat(member.score).isEqualTo(37.0)
    }

    @DisplayName("회원 평점은 100을 넘을 수 없다.")
    @Test
    fun updateScoreExceed() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )

        // when
        member.updateScore(100.1)

        // then
        assertThat(member.score).isEqualTo(100.0)
    }

    @DisplayName("회원 평점은 음수가 될 수 없다")
    @Test
    fun updateScoreNegative() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )

        // when
        member.updateScore(-36.1)

        // then
        assertThat(member.score).isEqualTo(0.0)
    }
}