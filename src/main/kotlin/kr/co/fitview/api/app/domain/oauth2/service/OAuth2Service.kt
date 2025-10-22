package kr.co.fitview.api.app.domain.oauth2.service

import kr.co.fitview.api.app.domain.auth.service.RefreshTokenService
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.oauth2.dto.request.OAuth2LoginServiceRequest
import kr.co.fitview.api.app.domain.oauth2.dto.request.OAuth2SignupServiceRequest
import kr.co.fitview.api.app.domain.oauth2.dto.response.OAuth2LoginResponse
import kr.co.fitview.api.app.global.entity.OAuth2Provider
import kr.co.fitview.api.app.global.jwt.JwtTokenProvider
import org.springframework.stereotype.Service


@Service
class OAuth2Service(
    private val appleService: AppleService,
    private val kakaoService: KakaoService,
    private val jwtTokenProvider : JwtTokenProvider,
    private val refreshTokenService : RefreshTokenService
) {

    fun loginWithAdd(request : OAuth2LoginServiceRequest) : OAuth2LoginResponse{

        var findMember : Member? = null

        if(request.provider == OAuth2Provider.APPLE){
            findMember = appleService.loginAppleWithAdd(request.toAppleServiceRequest())
        }

        else if(request.provider == OAuth2Provider.KAKAO) {
            findMember = kakaoService.loginKakaoWithAdd(request.toKakaoServiceRequest())
        }

        val accessToken = jwtTokenProvider.createAccessToken(findMember!!.id!!, findMember.role!!)
        val refreshToken = refreshTokenService.issueRefreshToken(findMember.id!!)

        return OAuth2LoginResponse(accessToken, refreshToken, findMember.isSignup!!)
    }

    fun signup(request: OAuth2SignupServiceRequest, memberId : Long) {


    }

}