package kr.co.fitview.api.app.domain.fcm.repository

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.fcm.entity.FcmToken
import kr.co.fitview.api.app.domain.fcm.entity.enums.FcmTokenPlatform
import kr.co.fitview.api.app.domain.fcm.entity.enums.FcmTokenStatus
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.global.entity.OAuth2Provider
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class FcmTokenRepositoryTest @Autowired constructor(
    private val memberRepository : MemberRepository,
    private val fcmTokenRepository : FcmTokenRepository,
    private val time : Time
) : IntegrationTestSupport() {

    @DisplayName("회원이 가진 전체 FCM 토큰을 조회한다.")
    @Test
    fun findAllByMemberIdAndStatus() {
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
        val findFcmTokens = fcmTokenRepository.findAllByMemberIdAndStatus(member.id!!, FcmTokenStatus.ACTIVE)

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
    fun findAllByMemberIdAndStatusOnlyActive() {
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
        fcmToken3.status = FcmTokenStatus.INVALID

        fcmTokenRepository.save(fcmToken1)
        fcmTokenRepository.save(fcmToken2)
        fcmTokenRepository.save(fcmToken3)

        // when
        val findFcmTokens = fcmTokenRepository.findAllByMemberIdAndStatus(member.id!!,FcmTokenStatus.ACTIVE)

        // then
        assertThat(findFcmTokens)
            .extracting("member", "deviceId", "token", "platform")
            .containsExactly(
                tuple(member, "deviceId1", "token1", FcmTokenPlatform.ANDROID),
                tuple(member, "deviceId2", "token2", FcmTokenPlatform.IOS),
            )

    }

    @DisplayName("deviceId로 FCM 토큰을 찾는다.")
    @Test
    fun findByDeviceIdAndStatus() {
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
        val findFcmTokens = fcmTokenRepository.findByDeviceIdAndStatus(fcmToken1.deviceId!!, FcmTokenStatus.ACTIVE)

        // then
        assertThat(findFcmTokens)
            .extracting("member", "deviceId", "token", "platform")
            .contains(
                member, "deviceId1", "token1", FcmTokenPlatform.ANDROID
            )

    }

    @DisplayName("기기의 fcm 토큰을 무효화한다.")
    @Test
    fun revokeAllFcmTokenBy() {
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
            token = "token2",
            platform = FcmTokenPlatform.IOS
        )
        fcmTokenRepository.save(fcmToken1)
        fcmTokenRepository.save(fcmToken2)
        fcmTokenRepository.save(fcmToken3)

        val deviceIds = listOf("deviceId1", "deviceId3")

        // when
        fcmTokenRepository.revokeAllFcmTokenBy(deviceIds)

        // then
        val findFcmTokens = fcmTokenRepository.findAll()

        assertThat(findFcmTokens)
            .extracting("id", "deviceId", "status")
            .contains(
                tuple(fcmToken1.id!!, fcmToken1.deviceId!!, FcmTokenStatus.REVOKED),
                tuple(fcmToken2.id!!, fcmToken2.deviceId!!, FcmTokenStatus.ACTIVE),
                tuple(fcmToken3.id!!, fcmToken3.deviceId!!, FcmTokenStatus.REVOKED),
            )
    }

    @DisplayName("회원이 지닌 Fcm 토큰을 전체 무효화한다.")
    @Test
    fun revokeAllFcmFrom() {
        // given
        val member1 = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE
        )
        memberRepository.save(member1)

        val fcmToken1 = FcmToken(
            member = member1,
            deviceId = "deviceId1",
            token = "token1",
            status = FcmTokenStatus.ACTIVE,
            platform = FcmTokenPlatform.ANDROID
        )
        val fcmToken2 = FcmToken(
            member = member1,
            deviceId = "deviceId2",
            token = "token2",
            status = FcmTokenStatus.ACTIVE,
            platform = FcmTokenPlatform.IOS
        )
        fcmTokenRepository.save(fcmToken1)
        fcmTokenRepository.save(fcmToken2)

        // when
        fcmTokenRepository.revokeAllFcmTokenBy(
            memberId = member1.id!!,
        )

        // then
        val fcmTokens = fcmTokenRepository.findAll()

        assertThat(fcmTokens).hasSize(2)

        assertThat(fcmTokens[0].status).isEqualTo(FcmTokenStatus.REVOKED)
        assertThat(fcmTokens[1].status).isEqualTo(FcmTokenStatus.REVOKED)
    }

    @DisplayName("다른 단말기에서 활성화된 동일한 fcm 토큰을 모두 찾는다.")
    @Test
    fun findByActiveFcmTokenAndOtherDeviceId() {
        // given
        val member1 = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE
        )
        memberRepository.save(member1)

        val fcmToken = "token"

        val fcmTokenEntity = FcmToken(
            member = member1,
            deviceId = "otherDeviceId",
            token = fcmToken,
            status = FcmTokenStatus.ACTIVE,
            platform = FcmTokenPlatform.ANDROID
        )
        fcmTokenRepository.save(fcmTokenEntity)

        val fcmTokenEntity2 = FcmToken(
            member = member1,
            deviceId = "otherDeviceId2",
            token = fcmToken,
            status = FcmTokenStatus.ACTIVE,
            platform = FcmTokenPlatform.ANDROID
        )
        fcmTokenRepository.save(fcmTokenEntity2)

        // when
        val findFcmTokens = fcmTokenRepository.findByActiveFcmTokenAndOtherDeviceId("deviceId", fcmToken)

        // then
        assertThat(findFcmTokens).hasSize(2)

        assertThat(findFcmTokens[0].id).isEqualTo(fcmTokenEntity.id!!)
        assertThat(findFcmTokens[1].id).isEqualTo(fcmTokenEntity2.id!!)
    }

    @DisplayName("다른 단말기에서 활성화된 fcm 토큰을 찾을 시, 본인 단말기에서 활성화된 fcm 토큰을 찾지 않는다.")
    @Test
    fun findByActiveFcmTokenAndOtherDeviceIdMeDeviceId() {
        // given
        val member1 = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE
        )
        memberRepository.save(member1)

        val fcmToken = "token"

        val fcmTokenEntity = FcmToken(
            member = member1,
            deviceId = "deviceId",
            token = fcmToken,
            status = FcmTokenStatus.ACTIVE,
            platform = FcmTokenPlatform.ANDROID
        )
        fcmTokenRepository.save(fcmTokenEntity)

        // when
        val findFcmTokenEntity = fcmTokenRepository.findByActiveFcmTokenAndOtherDeviceId("deviceId", fcmToken)

        // then
        assertThat(findFcmTokenEntity).hasSize(0)
    }
}