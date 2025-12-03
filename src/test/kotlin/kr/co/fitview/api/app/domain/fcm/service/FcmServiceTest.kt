package kr.co.fitview.api.app.domain.fcm.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.fcm.dto.request.FcmTokenCreateServiceRequest
import kr.co.fitview.api.app.domain.fcm.entity.FcmToken
import kr.co.fitview.api.app.domain.fcm.entity.enums.FcmTokenPlatform
import kr.co.fitview.api.app.domain.fcm.repository.FcmTokenRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.global.entity.Role
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class FcmServiceTest @Autowired constructor(
    private val fcmService: FcmService,
    private val fcmTokenRepository : FcmTokenRepository,
    private val memberRepository : MemberRepository
) : IntegrationTestSupport(){


    @DisplayName("회원 단말기의 fcm 토큰을 저장한다.")
    @Test
    fun addFcmToken() {
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
        fcmService.addFcmToken(member.id!!, request)

        // then
        val findFcmTokens = fcmTokenRepository.findAll()
        val findFcmToken = findFcmTokens[0]

        val findMember = memberRepository.findById(member.id!!).orElseThrow()

        assertThat(findFcmTokens).hasSize(1)
        assertThat(findFcmToken)
            .extracting("member", "deviceId", "token", "platform")
            .contains(findMember, "deviceId", "token", FcmTokenPlatform.ANDROID)
    }


}