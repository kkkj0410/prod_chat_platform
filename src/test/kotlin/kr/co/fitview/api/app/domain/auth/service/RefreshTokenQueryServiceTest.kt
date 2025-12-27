package kr.co.fitview.api.app.domain.auth.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.auth.entity.RefreshToken
import kr.co.fitview.api.app.domain.auth.entity.RefreshTokenStatus
import kr.co.fitview.api.app.domain.auth.repository.RefreshTokenRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class RefreshTokenQueryServiceTest @Autowired constructor(
    val refreshTokenQueryService: RefreshTokenQueryService,
    val refreshTokenRepository : RefreshTokenRepository,
    val memberRepository: MemberRepository,
    val oAuth2Service : OAuth2Service,
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
            uid = "id1",
            member = member,
            status = RefreshTokenStatus.ACTIVE,
            expiresAt = time.nowLocalDateTime.minusSeconds(1),
            deviceId = "deviceId1"
        )
        val refreshToken2 = RefreshToken(
            uid = "id2",
            member = member,
            status = RefreshTokenStatus.ACTIVE,
            expiresAt = time.nowLocalDateTime.minusSeconds(1),
            deviceId = "deviceId2"
        )
        val notExpireRefreshToken = RefreshToken(
            uid = "id3",
            member = member,
            status = RefreshTokenStatus.ACTIVE,
            expiresAt = time.nowLocalDateTime,
            deviceId = "deviceId3"
        )
        refreshTokenRepository.save(refreshToken1)
        refreshTokenRepository.save(refreshToken2)
        refreshTokenRepository.save(notExpireRefreshToken)

        // when
        val response = refreshTokenQueryService.findAllExpiredRefreshToken()

        // then
        assertThat(response)
            .extracting("refreshTokenUid", "deviceId")
            .contains(
                tuple(refreshToken1.uid, refreshToken1.deviceId!!),
                tuple(refreshToken2.uid, refreshToken2.deviceId!!),
            )
    }
}