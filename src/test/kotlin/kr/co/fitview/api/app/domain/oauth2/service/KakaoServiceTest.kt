package kr.co.fitview.api.app.domain.oauth2.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.oauth2.dto.request.KakaoLoginServiceRequest
import kr.co.fitview.api.app.domain.oauth2.dto.response.KakaoAccount
import kr.co.fitview.api.app.domain.oauth2.dto.response.KakaoProfile
import kr.co.fitview.api.app.global.entity.OAuth2Provider
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.member.MemberErrorCode
import kr.co.fitview.api.app.global.exception.error.network.NetworkErrorCode
import kr.co.fitview.api.app.global.exception.error.oauth2.OAuth2ErrorCode

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.ThrowingConsumer
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.beans.factory.annotation.Autowired

class KakaoServiceTest @Autowired constructor(
    val kakaoService: KakaoService,
    val memberRepository: MemberRepository,
) : IntegrationTestSupport(){


    @DisplayName("카카오 회원 프로필을 조회하여 회원 정보를 반환한다.")
    @Test
    fun loginKakaoWithAdd() {
        // given
        val email = "email"
        val providerId = "providerId"

        val member = Member(
            email = email,
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.KAKAO,
            providerId = providerId
        )
        val savedMember = memberRepository.save(member)

        val request = KakaoLoginServiceRequest(
            kakaoAccessToken = "kakaoAccessToken"
        )

        given(networkService.postKakaoProfile(any(), any()))
            .willReturn(
                KakaoProfile(
                    id = providerId,
                    kakao_account = KakaoAccount(email = email)
                )
            )

        // when
        val response = kakaoService.loginKakaoWithAdd(request)

        // then
        assertThat(response)
            .extracting("email", "password", "role", "provider", "providerId")
            .contains(savedMember.email, savedMember.password, savedMember.role, savedMember.provider, savedMember.providerId)
    }

    @DisplayName("카카오 회원 프로필을 조회하여, DB에 없으면 회원가입한다.")
    @Test
    fun loginKakaoWithAddWithoutMember() {
        val providerId = "providerId"
        val email = "email"

        val request = KakaoLoginServiceRequest(
            kakaoAccessToken = "kakaoAccessToken"
        )

        given(networkService.postKakaoProfile(any(), any()))
            .willReturn(
                KakaoProfile(
                    id = providerId,
                    kakao_account = KakaoAccount(email = email)
                )
            )

        // when
        val response = kakaoService.loginKakaoWithAdd(request)

        // then
        val findMember = memberRepository.findByProviderIdAndDeletedAtIsNull(providerId)

        assertThat(response)
            .extracting("id", "provider", "providerId", "email")
            .contains(findMember!!.id, findMember.provider, findMember.providerId, findMember.email)
    }


    @DisplayName("카카오 회원 프로필을 조회하여 회원가입 시, 로컬 회원이 이미 동일한 이메일을 사용 중이면 회원가입에 실패한다.")
    @Test
    fun loginKakaoWithAddDuplicatedEmail() {
        // given
        val email = "email"
        val providerId = "providerId"

        val member = Member(
            email = email,
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(member)

        val request = KakaoLoginServiceRequest(
            kakaoAccessToken = "kakaoAccessToken"
        )

        given(networkService.postKakaoProfile(any(), any()))
            .willReturn(
                KakaoProfile(
                    id = providerId,
                    kakao_account = KakaoAccount(email = email)
                )
            )

        // then
        assertThatThrownBy {
            kakaoService.loginKakaoWithAdd(request)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(MemberErrorCode.MEMBER_DUPLICATE_EMAIL)
            })
    }

    @DisplayName("카카오 회원 프로필 조회 실패하면 로그인을 중단한다.")
    @Test
    fun loginKakaoWithAddFailPost() {
        // given
        val request = KakaoLoginServiceRequest(
            kakaoAccessToken = "fail Access Token"
        )

        // when
        given(networkService.postKakaoProfile(any(), any()))
            .willThrow(GlobalException(NetworkErrorCode.NETWORK_SEND_ERROR) as Throwable)

        // then
        assertThatThrownBy {
            kakaoService.loginKakaoWithAdd(request)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(OAuth2ErrorCode.KAKAO_POST_FAILED)
            })
    }
}

