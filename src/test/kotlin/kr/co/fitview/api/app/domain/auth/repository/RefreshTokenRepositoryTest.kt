package kr.co.fitview.api.app.domain.auth.repository

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.auth.entity.RefreshToken
import kr.co.fitview.api.app.domain.auth.entity.RefreshTokenStatus
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.antlr.v4.runtime.tree.xpath.XPath.findAll
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional


@Transactional
class RefreshTokenRepositoryTest@Autowired constructor(
    val refreshTokenRepository : RefreshTokenRepository,
    val memberRepository : MemberRepository,
    val time : Time
) : IntegrationTestSupport() {

    @DisplayName("만료된 refreshToken 을 조회한다.")
    @Test
    fun findAllExpiredRefreshToken() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(member)

        val refreshToken1 = RefreshToken(
            id = "id1",
            member = member,
            status = RefreshTokenStatus.ACTIVE,
            expiresAt = time.nowLocalDateTime.minusSeconds(1),
            deviceId = "deviceId1"
        )
        val refreshToken2 = RefreshToken(
            id = "id2",
            member = member,
            status = RefreshTokenStatus.ACTIVE,
            expiresAt = time.nowLocalDateTime.minusSeconds(1),
            deviceId = "deviceId2"
        )
        val notExpireRefreshToken = RefreshToken(
            id = "id3",
            member = member,
            status = RefreshTokenStatus.ACTIVE,
            expiresAt = time.nowLocalDateTime,
            deviceId = "deviceId3"
        )
        refreshTokenRepository.save(refreshToken1)
        refreshTokenRepository.save(refreshToken2)
        refreshTokenRepository.save(notExpireRefreshToken)

        // when
        val response = refreshTokenRepository.findAllExpiredRefreshToken()

        // then
        assertThat(response)
            .extracting("refreshTokenId", "deviceId")
            .contains(
                tuple(refreshToken1.id!!, refreshToken1.deviceId!!),
                tuple(refreshToken2.id!!, refreshToken2.deviceId!!),
            )
    }

    @DisplayName("refreshToken을 비활성화한다.")
    @Test
    fun updateAllInactive() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(member)

        val refreshToken1 = RefreshToken(
            id = "id1",
            member = member,
            status = RefreshTokenStatus.ACTIVE,
            expiresAt = time.nowLocalDateTime,
            deviceId = "deviceId1"
        )
        val refreshToken2 = RefreshToken(
            id = "id2",
            member = member,
            status = RefreshTokenStatus.ACTIVE,
            expiresAt = time.nowLocalDateTime,
            deviceId = "deviceId1"
        )
        refreshTokenRepository.save(refreshToken1)
        refreshTokenRepository.save(refreshToken2)

        val refreshTokenIds = listOf(refreshToken1.id!!, refreshToken2.id!!)

        // when
        refreshTokenRepository.updateAllInactive(refreshTokenIds)

        // then
        val refreshTokens = refreshTokenRepository.findAll()

        assertThat(refreshTokens).hasSize(2)
        assertThat(refreshTokens[0].status).isEqualTo(RefreshTokenStatus.INACTIVE)
        assertThat(refreshTokens[1].status).isEqualTo(RefreshTokenStatus.INACTIVE)
    }

}