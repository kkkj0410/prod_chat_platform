package kr.co.fitview.api.app.domain.invitation.service

import kr.co.fitview.api.app.IntegrationTestSupport
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class InvitationCodeProviderTest @Autowired constructor(
    private val invitationCodeProvider: InvitationCodeProvider
) : IntegrationTestSupport() {

    @DisplayName("encode 후 decode 하면 원래 PK가 반환된다")
    @Test
    fun encodeDecodeRoundTrip() {
        // given
        val pk = 12345L

        // when
        val decoded = invitationCodeProvider.decode(invitationCodeProvider.encode(pk))

        // then
        assertThat(decoded).isEqualTo(pk)
    }

    @DisplayName("0~999999 범위의 모든 PK는 encode 결과가 서로 겹치지 않는다")
    @Test
    fun encodeNoCollisionInRange() {
        // given
        val encoded = (0L..999_999L).map { invitationCodeProvider.encode(it) }

        // when
        val unique = encoded.toSet()

        // then
        assertThat(unique.size).isEqualTo(1_000_000)
    }

    @DisplayName("1,000,000 이상의 PK는 범위 내 PK와 encode 결과가 충돌한다")
    @Test
    fun encodeCollisionOverLimit() {
        // given
        val inRange = invitationCodeProvider.encode(0L)
        val overLimit = invitationCodeProvider.encode(1_000_000L)

        // then
        assertThat(inRange).isEqualTo(overLimit)
    }

    @DisplayName("encode 결과는 항상 숫자 6자리이다")
    @Test
    fun encodeReturns6Digits() {
        // given
        val pk = 12345L

        // when
        val code = invitationCodeProvider.encode(pk)

        // then
        assertThat(code).hasSize(6)
        assertThat(code).matches("[0-9]{6}")
    }

}