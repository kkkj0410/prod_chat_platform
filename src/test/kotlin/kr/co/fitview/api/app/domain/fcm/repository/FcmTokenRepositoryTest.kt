package kr.co.fitview.api.app.domain.fcm.repository

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.fcm.entity.FcmToken
import kr.co.fitview.api.app.domain.fcm.entity.enums.FcmTokenPlatform
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.global.entity.Role
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class FcmTokenRepositoryTest @Autowired constructor(
    private val memberRepository : MemberRepository,
    private val fcmTokenRepository : FcmTokenRepository,
) : IntegrationTestSupport() {

    @DisplayName("회원이 가진 전체 FCM 토큰을 조회한다.")
    @Test
    fun findAllByMemberIdAndIsActiveTrueAndDeletedAtIsNull() {
        // given
        val member = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member)

        val fcmToken1 = FcmToken.of(
            member = member,
            deviceId = "deviceId1",
            token = "token1",
            platform = FcmTokenPlatform.ANDROID
        )
        val fcmToken2 = FcmToken.of(
            member = member,
            deviceId = "deviceId2",
            token = "token2",
            platform = FcmTokenPlatform.IOS
        )
        fcmTokenRepository.save(fcmToken1)
        fcmTokenRepository.save(fcmToken2)

        // when
        val findFcmTokens = fcmTokenRepository.findAllByMemberIdAndIsActiveTrueAndDeletedAtIsNull(member.id!!)

        // then
        assertThat(findFcmTokens)
            .extracting("member", "deviceId", "token", "platform")
            .containsExactly(
                tuple(member, "deviceId1", "token1", FcmTokenPlatform.ANDROID),
                tuple(member, "deviceId2", "token2", FcmTokenPlatform.IOS),
            )
    }

    @DisplayName("회원이 가진 전체 FCM 토큰을 조회하되, 활성화되어있는 토큰만 가져온다.")
    @Test
    fun findAllByMemberIdAndIsActiveTrueAndDeletedAtIsNullOnlyActiveIsTrue() {
        // given
        val member = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member)

        val fcmToken1 = FcmToken.of(
            member = member,
            deviceId = "deviceId1",
            token = "token1",
            platform = FcmTokenPlatform.ANDROID
        )
        val fcmToken2 = FcmToken.of(
            member = member,
            deviceId = "deviceId2",
            token = "token2",
            platform = FcmTokenPlatform.IOS
        )
        val fcmToken3 = FcmToken.of(
            member = member,
            deviceId = "deviceId3",
            token = "token3",
            platform = FcmTokenPlatform.ANDROID
        )
        fcmToken3.isActive = false

        fcmTokenRepository.save(fcmToken1)
        fcmTokenRepository.save(fcmToken2)
        fcmTokenRepository.save(fcmToken3)

        // when
        val findFcmTokens = fcmTokenRepository.findAllByMemberIdAndIsActiveTrueAndDeletedAtIsNull(member.id!!)

        // then
        assertThat(findFcmTokens)
            .extracting("member", "deviceId", "token", "platform")
            .containsExactly(
                tuple(member, "deviceId1", "token1", FcmTokenPlatform.ANDROID),
                tuple(member, "deviceId2", "token2", FcmTokenPlatform.IOS),
            )

    }
}