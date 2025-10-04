package kr.co.fitview.api.app.global.entity

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.global.constant.JwtConstant.ROLE
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.jwt.JwtErrorCode
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class RoleTest : IntegrationTestSupport(){

    @DisplayName("역할이 매칭되면 Role로 변환한다.")
    @Test
    fun from() {
        // given
        val role = "USER"

        // when
        val findRole = Role.from(role)

        // then
        assertThat(findRole).isEqualTo(Role.USER)
    }

    @DisplayName("정의되지 않은 역할은 Role로 변환하지 못한다.")
    @CsvSource("user", "User", "customer")
    @ParameterizedTest
    fun fromWithNotRole(role : String) {
        //given & when & then
        assertThatThrownBy {
            Role.from(role)
        }
        .isInstanceOf(IllegalArgumentException::class.java)
        .hasMessage("매칭되는 역할이 없습니다.")

    }
}