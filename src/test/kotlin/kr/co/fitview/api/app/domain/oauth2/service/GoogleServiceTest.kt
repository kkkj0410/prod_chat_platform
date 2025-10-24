package kr.co.fitview.api.app.domain.oauth2.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.oauth2.dto.request.GoogleLoginServiceRequest
import kr.co.fitview.api.app.global.entity.OAuth2Provider
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.oauth2.OAuth2ErrorCode
import kr.co.fitview.api.app.global.id.IdGenerator
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.ThrowingConsumer
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.beans.factory.annotation.Autowired

class GoogleServiceTest @Autowired constructor(
    val memberRepository : MemberRepository,
    val googleService : GoogleService,
    val idGenerator : IdGenerator
) : IntegrationTestSupport(){

    @DisplayName("소셜 로그인에 성공한 회원을 조회한다.")
    @Test
    fun loginGoogleWithAdd() {
        // given
        val email = "email"
        val providerId = "providerId"

        val member = Member(
            email = email,
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.GOOGLE,
            providerId = providerId
        )
        memberRepository.save(member)

        val request = GoogleLoginServiceRequest(
            googleAuthCode = "googleAuthCode"
        )

        given(networkService.postByWebClient(any(), any()))
            .willReturn(
                mapOf(
                    "access_token" to "googleAccessToken"
                )
            )

        given(networkService.getByWebClient(any(), any()))
            .willReturn(
                mapOf(
                    "id" to providerId,
                    "email" to email
                )
            )

        // when
        val findMember = googleService.loginGoogleWithAdd(request)

        // then
        assertThat(findMember.id).isNotNull()
        assertThat(findMember)
            .extracting("email", "provider", "providerId")
            .contains(email, OAuth2Provider.GOOGLE, providerId)

    }

    @DisplayName("소셜 로그인에 성공한 회원에 대한 정보가 DB에 없으면 DB에 저장한다.")
    @Test
    fun loginGoogleWithAddWithoutMember() {
        // given
        val email = "email"
        val providerId = "providerId"

        val request = GoogleLoginServiceRequest(
            googleAuthCode = "googleAuthCode"
        )

        given(networkService.postByWebClient(any(), any()))
            .willReturn(
                mapOf(
                    "access_token" to "googleAccessToken"
                )
            )

        given(networkService.getByWebClient(any(), any()))
            .willReturn(
                mapOf(
                    "id" to providerId,
                    "email" to email
                )
            )

        // when
        val findMember = googleService.loginGoogleWithAdd(request)

        // then
        assertThat(findMember.id).isNotNull()
        assertThat(findMember)
            .extracting("email", "provider", "providerId", "password", "role")
            .contains(email, OAuth2Provider.GOOGLE, providerId, idGenerator.createUuid(), Role.USER)
    }

    @DisplayName("소셜 로그인 시, authCode로 accessToken 조회 실패")
    @Test
    fun loginGoogleWithAddFailedPostAccessToken() {
        // given
        val request = GoogleLoginServiceRequest(
            googleAuthCode = "InvalidAuthCode"
        )

        given(networkService.postByWebClient(any(), any()))
            .willThrow(RuntimeException())

        // when & then
        assertThatThrownBy {
            googleService.loginGoogleWithAdd(request)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(OAuth2ErrorCode.GOOGLE_AUTH_CODE_EXCHANGE_FAILED)
            })
    }

    @DisplayName("소셜 로그인 시, accessToken 요청 응답에 성공했으나, accessToken 필드가 없으면 프로필 조회 실패")
    @Test
    fun loginGoogleWithAddWithoutAccessToken() {
        // given
        val request = GoogleLoginServiceRequest(
            googleAuthCode = "googleAuthCode"
        )

        given(networkService.postByWebClient(any(), any()))
            .willReturn(
                mapOf(
                )
            )

        //when & then
        assertThatThrownBy {
            googleService.loginGoogleWithAdd(request)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(OAuth2ErrorCode.GOOGLE_ACCESS_TOKEN_PARSE_FAILED)
            })
    }

    @DisplayName("소셜 로그인 시, 액세스 토큰으로 프로필 조회를 시도했으나, 요청 실패")
    @Test
    fun loginGoogleWithAddFailedPostProfile() {
        // given
        val request = GoogleLoginServiceRequest(
            googleAuthCode = "InvalidAuthCode"
        )

        given(networkService.postByWebClient(any(), any()))
            .willReturn(
                mapOf(
                    "access_token" to "googleAccessToken"
                )
            )

        given(networkService.getByWebClient(any(), any()))
            .willThrow(RuntimeException())

        // when & then
        assertThatThrownBy {
            googleService.loginGoogleWithAdd(request)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(OAuth2ErrorCode.GOOGLE_PROFILE_REQUEST_FAILED)
            })
    }

    @DisplayName("소셜 로그인 시, 프로필 응답에서 구글 고유 id 파싱에 실패하면 프로필 조회 불가")
    @Test
    fun loginGoogleWithAddWithoutId() {
        // given
        val email = "email"

        val request = GoogleLoginServiceRequest(
            googleAuthCode = "googleAuthCode"
        )

        given(networkService.postByWebClient(any(), any()))
            .willReturn(
                mapOf(
                    "access_token" to "googleAccessToken"
                )
            )

        given(networkService.getByWebClient(any(), any()))
            .willReturn(
                mapOf(
                    "email" to email
                )
            )

        // when & then
        assertThatThrownBy {
            googleService.loginGoogleWithAdd(request)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(OAuth2ErrorCode.GOOGLE_PROFILE_ID_MISSING)
            })
    }


    @DisplayName("소셜 로그인 시, 프로필 응답에서 구글 고유 email 파싱에 실패하면 프로필 조회 불가")
    @Test
    fun loginGoogleWithAddWithoutEmail() {
        // given
        val googleId = "googleId"

        val request = GoogleLoginServiceRequest(
            googleAuthCode = "googleAuthCode"
        )

        given(networkService.postByWebClient(any(), any()))
            .willReturn(
                mapOf(
                    "access_token" to "googleAccessToken"
                )
            )

        given(networkService.getByWebClient(any(), any()))
            .willReturn(
                mapOf(
                    "id" to googleId
                )
            )

        // when & then
        assertThatThrownBy {
            googleService.loginGoogleWithAdd(request)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(OAuth2ErrorCode.GOOGLE_PROFILE_EMAIL_MISSING)
            })
    }



}