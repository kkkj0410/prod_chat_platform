package kr.co.fitview.api.app.domain.auth.entity

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.global.entity.Role
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class RefreshTokenTest : IntegrationTestSupport(){

    @DisplayName("리프레시 토큰을 비활성화한다.")
    @Test
    fun setRevoke() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )

        val refreshTokenEntity = RefreshToken(
            "uuid",
            member,
            RefreshTokenStatus.ACTIVE
        )

        // when
        refreshTokenEntity.setRevoke()

        // then
        assertThat(refreshTokenEntity.status).isEqualTo(RefreshTokenStatus.REVOKED)

    }
}