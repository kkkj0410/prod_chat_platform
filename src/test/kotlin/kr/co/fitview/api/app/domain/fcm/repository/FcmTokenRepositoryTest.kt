package kr.co.fitview.api.app.domain.fcm.repository

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.fcm.entity.FcmToken
import kr.co.fitview.api.app.domain.fcm.entity.enums.FcmTokenPlatform
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.global.entity.OAuth2Provider
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import kr.co.fitview.api.app.global.time.TimeHolder.time
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

    @DisplayName("deviceId로 FCM 토큰을 찾는다.")
    @Test
    fun findByDeviceIdAndIsActiveTrueAndDeletedAtIsNull() {
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
        val findFcmTokens = fcmTokenRepository.findByDeviceIdAndIsActiveTrueAndDeletedAtIsNull(fcmToken1.deviceId!!)

        // then
        assertThat(findFcmTokens)
            .extracting("member", "deviceId", "token", "platform")
            .contains(
                member, "deviceId1", "token1", FcmTokenPlatform.ANDROID
            )

    }

    @DisplayName("기기의 fcm 토큰을 삭제한다.")
    @Test
    fun deleteAllFcmTokenBy() {
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
        fcmTokenRepository.deleteAllFcmTokenBy(deviceIds)

        // then
        val findFcmTokens = fcmTokenRepository.findAll()

        assertThat(findFcmTokens)
            .extracting("id", "deviceId", "deletedAt")
            .contains(
                tuple(fcmToken1.id!!, fcmToken1.deviceId!!, time.nowLocalDateTime),
                tuple(fcmToken2.id!!, fcmToken2.deviceId!!, null),
                tuple(fcmToken3.id!!, fcmToken3.deviceId!!, time.nowLocalDateTime),
            )
    }

    @DisplayName("회원이 지닌 Fcm 토큰을 전체 삭제한다.")
    @Test
    fun deleteAllFcmFrom() {
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
            isActive = true,
            platform = FcmTokenPlatform.ANDROID
        )
        val fcmToken2 = FcmToken(
            member = member1,
            deviceId = "deviceId2",
            token = "token2",
            isActive = true,
            platform = FcmTokenPlatform.IOS
        )
        fcmTokenRepository.save(fcmToken1)
        fcmTokenRepository.save(fcmToken2)

        // when
        fcmTokenRepository.deleteAllFcmTokenBy(
            memberId = member1.id!!,
        )

        // then
        val fcmTokens = fcmTokenRepository.findAll()

        assertThat(fcmTokens).hasSize(2)

        assertThat(fcmTokens[0].deletedAt).isNotNull()
        assertThat(fcmTokens[1].deletedAt).isNotNull()
    }

}