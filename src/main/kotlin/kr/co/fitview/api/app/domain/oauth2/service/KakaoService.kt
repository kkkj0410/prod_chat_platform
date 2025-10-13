package kr.co.fitview.api.app.domain.oauth2.service

import kr.co.fitview.api.app.domain.auth.service.RefreshTokenService
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.service.MemberService
import kr.co.fitview.api.app.domain.oauth2.dto.request.KakaoLoginServiceRequest
import kr.co.fitview.api.app.domain.oauth2.dto.response.AppleProfile
import kr.co.fitview.api.app.domain.oauth2.dto.response.KakaoProfile
import kr.co.fitview.api.app.domain.oauth2.dto.response.OAuth2LoginResponse
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
    val refreshTokenService: RefreshTokenService
) {
    val requestUrlFromKakao = "https://kapi.kakao.com/v2/user/me"

    @Transactional
    fun loginKakaoWithSignup(request: KakaoLoginServiceRequest): OAuth2LoginResponse {

        val kakaoProfile = getKakaoProfile(request)

        var findMember = memberService.findMemberFromProviderId(kakaoProfile.id)

        if(isNull(findMember)){
            findMember = kakaoSignup(kakaoProfile)
        }

        val accessToken = jwtTokenProvider.createAccessToken(findMember!!.id!!, findMember.role!!)
        val refreshToken = refreshTokenService.issueRefreshToken(findMember.id!!)

        return OAuth2LoginResponse(accessToken,refreshToken)
    }


    private fun getKakaoProfile(request: KakaoLoginServiceRequest): KakaoProfile {

        try{
            val kakaoProfile = networkService.postKakaoProfile(
                url = requestUrlFromKakao,
                accessToken = request.kakaoAccessToken
            )
            return kakaoProfile
        }
        catch(ex : Exception){
            throw GlobalException(OAuth2ErrorCode.KAKAO_POST_FAILED)
        }
    }

    private fun isNull(member: Member?) = member == null


    private fun kakaoSignup(
        kakaoProfile: KakaoProfile,
    ): Member {
        val member = createKakaoMember(kakaoProfile.id, kakaoProfile.kakao_account.email)
        return memberService.addMember(member)
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