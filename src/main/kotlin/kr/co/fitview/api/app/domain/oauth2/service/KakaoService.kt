package kr.co.fitview.api.app.domain.oauth2.service

import kr.co.fitview.api.app.domain.auth.service.RefreshTokenService
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.service.MemberService
import kr.co.fitview.api.app.domain.oauth2.dto.request.KakaoLoginServiceRequest
import kr.co.fitview.api.app.domain.oauth2.dto.response.KakaoProfile
import kr.co.fitview.api.app.global.entity.OAuth2Provider
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.oauth2.OAuth2ErrorCode
import kr.co.fitview.api.app.global.id.IdGenerator
import kr.co.fitview.api.app.global.jwt.JwtTokenProvider
import kr.co.fitview.api.app.global.network.NetworkService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class KakaoService(
    val networkService: NetworkService,
    val memberService : MemberService,
    val idGenerator: IdGenerator,
    val jwtTokenProvider: JwtTokenProvider,
) {
    val requestUrlFromKakao = "https://kapi.kakao.com/v2/user/me"

    @Transactional
    fun loginKakaoWithAdd(request: KakaoLoginServiceRequest): Member {

        val kakaoProfile = getKakaoProfile(request)

        var findMember = memberService.findMemberFromProviderId(kakaoProfile.id)

        if(isNull(findMember)){
            findMember = kakaoSignup(kakaoProfile)
        }

        return findMember!!
    }


    private fun getKakaoProfile(request: KakaoLoginServiceRequest): KakaoProfile {

        val header = createAccessTokenHeader(request.kakaoAccessToken)

        val response = try {
            networkService.getByWebClient(requestUrlFromKakao, header)
        } catch (e: Exception) {
            throw GlobalException(OAuth2ErrorCode.KAKAO_POST_FAILED)
        }

        val kakaoId = (response["id"] as Long).toString()
        val kakaoEmail = (response["kakao_account"] as Map<*, *>)["email"] as String

        return KakaoProfile(kakaoId, kakaoEmail)
    }

    private fun createAccessTokenHeader(accessToken: String): Map<String, String> {
        val headers = mapOf("Authorization" to "Bearer $accessToken")
        return headers
    }

    private fun isNull(member: Member?) = member == null


    private fun kakaoSignup(
        kakaoProfile: KakaoProfile,
    ): Member {
        val member = createKakaoMember(kakaoProfile.id, kakaoProfile.email)
        return memberService.addMemberByOAuth2(member)
    }

    private fun createKakaoMember(providerId: String, email: String)
    = Member(
        provider = OAuth2Provider.KAKAO,
        providerId = providerId,
        email = email,
        password = idGenerator.createUuid(),
        role = Role.USER
    )
}