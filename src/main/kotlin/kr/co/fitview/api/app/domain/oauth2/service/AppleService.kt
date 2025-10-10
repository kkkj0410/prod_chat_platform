package kr.co.fitview.api.app.domain.oauth2.service

import com.nimbusds.jose.JWSVerifier
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import kr.co.fitview.api.app.domain.auth.service.RefreshTokenService
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.service.MemberService
import kr.co.fitview.api.app.domain.oauth2.config.AppleConfig
import kr.co.fitview.api.app.domain.oauth2.dto.request.AppleLoginRequest
import kr.co.fitview.api.app.domain.oauth2.dto.response.AppleProfile
import kr.co.fitview.api.app.domain.oauth2.dto.response.OAuth2LoginResponse
import kr.co.fitview.api.app.domain.oauth2.util.AppleJwtProvider
import kr.co.fitview.api.app.global.entity.OAuth2Provider
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.oauth2.OAuth2ErrorCode
import kr.co.fitview.api.app.global.id.IdGenerator
import kr.co.fitview.api.app.global.jwt.JwtTokenProvider
import kr.co.fitview.api.app.global.network.NetworkService
import kr.co.fitview.api.app.global.time.Time
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

import java.net.URI


@Service
@Transactional(readOnly = true)
class AppleService(
    val appleConfig : AppleConfig,
    val appleJwtProvider : AppleJwtProvider,
    val appleAuthService: AppleAuthService,
    val networkService: NetworkService,
    val memberService : MemberService,
    val jwtTokenProvider : JwtTokenProvider,
    val refreshTokenService : RefreshTokenService,
    val idGenerator: IdGenerator,
    val time : Time
) {
    val appleEndpointUrl = "https://appleid.apple.com/auth/token"

    @Transactional
    fun loginAppleWithSignup(
        request : AppleLoginRequest
    ) : OAuth2LoginResponse{

        val appleJwtToken = getAppleJwtToken(request)

        val appleProfile = appleAuthService.extractAppleProfileWithValidate(appleJwtToken)

        var findMember = memberService.findMemberFromProviderId(appleProfile.appleId)

        if(isNull(findMember)){
            validateEmail(appleProfile.email)

            findMember = appleSignup(appleProfile)
        }

        val accessToken = jwtTokenProvider.createAccessToken(findMember!!.id!!, findMember.role!!)
        val refreshToken = refreshTokenService.issueRefreshToken(findMember.id!!)

        return OAuth2LoginResponse(accessToken, refreshToken)
    }

    private fun getAppleJwtToken(request: AppleLoginRequest): String {
        val response = getApplePostResponse(request)

        return response["id_token"] as? String ?: throw GlobalException(OAuth2ErrorCode.APPLE_JWT_NOT_FOUND)
    }

    private fun getApplePostResponse(request: AppleLoginRequest): Map<String, Any> {
        val clientSecret = appleJwtProvider.createClientSecret()

        val formData: MultiValueMap<String, String> = getFormData(clientSecret, request)

        return postOauth2Apple(formData)
    }

    private fun getFormData(
        clientSecret: String,
        request: AppleLoginRequest
    ): MultiValueMap<String, String> {
        val formData: MultiValueMap<String, String> = LinkedMultiValueMap()
        formData.add("client_id", appleConfig.clientId)
        formData.add("client_secret", clientSecret)
        formData.add("code", request.appleAuthCode)
        formData.add("grant_type", "authorization_code")
        formData.add("redirect_uri", request.redirectUri)
        return formData
    }

    private fun postOauth2Apple(formData: MultiValueMap<String, String>): Map<String, Any> {
        try {
            return networkService.postByWebClient(formData, appleEndpointUrl)
        } catch (e: Exception) {
            throw GlobalException(OAuth2ErrorCode.APPLE_POST_FAILED)
        }
    }

    private fun validateEmail(email : String?) {
        if (isNull(email)) {
            throw GlobalException(OAuth2ErrorCode.APPLE_EMAIL_NOT_FOUND)
        }
    }

    private fun appleSignup(
        appleProfile: AppleProfile,
    ): Member {
        val member = createAppleMember(appleProfile.appleId, appleProfile.email!!)
        return memberService.addMember(member)
    }


    private fun createAppleMember(
        providerId : String,
        email : String
    ) = Member(
        provider = OAuth2Provider.APPLE,
        providerId = providerId,
        email = email,
        password = idGenerator.createUuid(),
        role = Role.USER
    )



    private fun <T> isNull(obj: T?) = obj == null
}