package kr.co.fitview.api.app.domain.oauth2.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.auth.dto.response.MemberLoginResponse
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.member.service.MemberService
import kr.co.fitview.api.app.domain.oauth2.dto.request.AppleLoginRequest
import kr.co.fitview.api.app.domain.oauth2.dto.response.AppleProfile
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.id.IdGenerator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.willReturn
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.beans.factory.annotation.Autowired

class AppleServiceTest @Autowired constructor(
    val appleService : AppleService,
    val memberRepository : MemberRepository,
    val idGenerator : IdGenerator
) : IntegrationTestSupport(){


    @DisplayName("애플 인증 코드를 받고 프로필을 조회했을때, 없는 회원이면 회원가입 후 로그인한다.")
    @Test
    fun loginAppleWithSignup() {
        // given
        val request = AppleLoginRequest(
            appleAuthCode = "appleAuthCode",
            redirectUri = "https://asd.com"
        )

        given(networkService.postByWebClient(any(), any()))
            .willReturn(
                mapOf(
                    "id_token" to "appleJwtToken"
                )
            )

        given(appleAuthService.extractAppleProfileWithValidate(any()))
            .willReturn(
                AppleProfile(
                    appleId = "appleId",
                    email = "email"
                )
            )

        // when
        appleService.loginAppleWithSignup(request)

        // then
        val findMember = memberRepository.findByProviderIdAndDeletedAtIsNull("appleId")
        assertThat(findMember!!.id).isNotNull()
        assertThat(findMember)
            .extracting("email", "password", "role")
            .contains(findMember.email, idGenerator.createUuid(), Role.USER)

    }

}