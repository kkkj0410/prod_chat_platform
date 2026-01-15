package kr.co.fitview.api.app.domain.fcm.service

import jakarta.persistence.EntityManager
import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.fcm.dto.request.FcmTokenCreateServiceRequest
import kr.co.fitview.api.app.domain.fcm.entity.FcmToken
import kr.co.fitview.api.app.domain.fcm.entity.enums.FcmTokenPlatform
import kr.co.fitview.api.app.domain.fcm.repository.FcmTokenRepository
import kr.co.fitview.api.app.domain.member.dto.request.MemberWithdrawServiceRequest
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.entity.MemberWithdrawReason
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWithdrawReasonReasonType
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.global.entity.OAuth2Provider
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.TimeHolder.time
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class FcmTokenServiceTest @Autowired constructor(
    private val fcmTokenService: FcmTokenService,
    private val fcmTokenRepository : FcmTokenRepository,
    private val memberRepository : MemberRepository,
    private val em : EntityManager,
    private val fcmPublisher : FcmPublisher
) : IntegrationTestSupport(){

    @DisplayName("")
    @Test
    fun test() {
        // given

        val map = mapOf(
            "type" to "CHAT_MESSAGE",
            "deepLink" to "myapp://chat/room_1234"
        )

        fcmPublisher.send(
            token = "eLwnjbKiQ5qAg4-Gth-CoK:APA91bGI0LzyPLtWMc4iQEGaL37wtzjsnXhdRDX3ThtdtWQXp3GNzGzlgrtYC8teSwJDKAshlmwlClP0OGwUlyAcQiw1Am6Pb1b7DpqQrCHACuNcxaskLds",
            title = "hello2",
            body = "body2",
            platform = FcmTokenPlatform.ANDROID,
            data = map
        )
        // when

        // then

    }


    @DisplayName("회원 단말기의 fcm 토큰을 저장한다.")
    @Test
    fun saveFcmToken() {
        // given
        val member = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member)

        val request = FcmTokenCreateServiceRequest(
            deviceId = "deviceId",
            token = "token",
            platform = FcmTokenPlatform.ANDROID
        )

        // when
        fcmTokenService.saveFcmToken(member.id!!, request)

        // then
        val findFcmTokens = fcmTokenRepository.findAll()
        val findFcmToken = findFcmTokens[0]

        val findMember = memberRepository.findById(member.id!!).orElseThrow()

        assertThat(findFcmTokens).hasSize(1)
        assertThat(findFcmToken)
            .extracting("member", "deviceId", "token", "platform")
            .contains(findMember, "deviceId", "token", FcmTokenPlatform.ANDROID)
    }

    @DisplayName("fcm 토큰 저장 시, deviceId가 DB에 동일한게 있다면 해당 fcm 토큰을 업데이트한다.")
    @Test
    fun saveFcmTokenDuplicatedDeviceId() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(member)

        val fcmToken = FcmToken.of(
            member = member,
            deviceId = "deviceId",
            token = "token",
            platform = FcmTokenPlatform.ANDROID
        )
        fcmTokenRepository.save(fcmToken)

        val request = FcmTokenCreateServiceRequest(
            deviceId = "deviceId",
            token = "updateToken",
            platform = FcmTokenPlatform.ANDROID
        )

        // when
        fcmTokenService.saveFcmToken(member.id!!, request)

        // then
        val findFcmTokens = fcmTokenRepository.findAll()
        val findFcmToken = findFcmTokens[0]

        val findMember = memberRepository.findById(member.id!!).orElseThrow()

        assertThat(findFcmTokens).hasSize(1)
        assertThat(findFcmToken)
            .extracting("member", "deviceId", "token", "platform")
            .contains(findMember, "deviceId", "updateToken", FcmTokenPlatform.ANDROID)
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
        fcmTokenService.modifyAllFcmTokenFrom(deviceIds)

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
        fcmTokenService.deleteAllFcmFrom(
            memberId = member1.id!!,
        )

        // then
        val fcmTokens = fcmTokenRepository.findAll()

        assertThat(fcmTokens).hasSize(2)

        assertThat(fcmTokens[0].deletedAt).isNotNull()
        assertThat(fcmTokens[1].deletedAt).isNotNull()
    }



}