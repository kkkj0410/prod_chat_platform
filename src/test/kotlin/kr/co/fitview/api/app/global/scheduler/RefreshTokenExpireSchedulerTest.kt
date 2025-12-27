package kr.co.fitview.api.app.global.scheduler

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.auth.entity.RefreshToken
import kr.co.fitview.api.app.domain.auth.entity.RefreshTokenStatus
import kr.co.fitview.api.app.domain.auth.repository.RefreshTokenRepository
import kr.co.fitview.api.app.domain.fcm.entity.FcmToken
import kr.co.fitview.api.app.domain.fcm.entity.enums.FcmTokenPlatform
import kr.co.fitview.api.app.domain.fcm.repository.FcmTokenRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class RefreshTokenExpireSchedulerTest @Autowired constructor(
    private val memberRepository: MemberRepository,
    private val fcmTokenRepository: FcmTokenRepository,
    private val refreshTokenRepository : RefreshTokenRepository,
    private val refreshTokenExpireScheduler: RefreshTokenExpireScheduler,
    private val time: Time,

    ) : IntegrationTestSupport() {

    @DisplayName("리프레시 토큰을 만료시킨다.")
    @Test
    fun modifyAllExpireFcm() {
        // given
        val member = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
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
            deviceId = "deviceId100"
        )
        refreshTokenRepository.save(refreshToken1)
        refreshTokenRepository.save(refreshToken2)

        val fcmToken1 = FcmToken.of(
            member = member,
            deviceId = "deviceId1",
            token = "token1",
            platform = FcmTokenPlatform.ANDROID
        )
        val fcmToken2 = FcmToken.of(
            member = member,
            deviceId = "deviceId1",
            token = "token2",
            platform = FcmTokenPlatform.IOS
        )
        val fcmToken3 = FcmToken.of(
            member = member,
            deviceId = "deviceId3",
            token = "token2",
            platform = FcmTokenPlatform.IOS
        )
        fcmTokenRepository.save(fcmToken1)
        fcmTokenRepository.save(fcmToken2)
        fcmTokenRepository.save(fcmToken3)

        // when
        refreshTokenExpireScheduler.modifyAllExpireFcm()

        // then
        val findRefreshTokens = refreshTokenRepository.findAll()
        assertThat(findRefreshTokens).hasSize(2)
        assertThat(findRefreshTokens[0].status).isEqualTo(RefreshTokenStatus.INACTIVE)
        assertThat(findRefreshTokens[1].status).isEqualTo(RefreshTokenStatus.INACTIVE)

        val findFcmTokens = fcmTokenRepository.findAll()
        assertThat(findFcmTokens)
            .extracting("id", "deviceId", "deletedAt")
            .contains(
                tuple(fcmToken1.id!!, fcmToken1.deviceId!!, time.nowLocalDateTime),
                tuple(fcmToken2.id!!, fcmToken2.deviceId!!, time.nowLocalDateTime),
            )
    }
}