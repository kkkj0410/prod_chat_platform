package kr.co.fitview.api.app.domain.oauth2.service

import kr.co.fitview.api.app.domain.auth.service.RefreshTokenService
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.domain.member.service.MemberService
import kr.co.fitview.api.app.domain.oauth2.config.AppleConfig
import kr.co.fitview.api.app.domain.oauth2.dto.request.AppleLoginServiceRequest
import kr.co.fitview.api.app.domain.oauth2.dto.response.AppleProfile
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


@Service
@Transactional(readOnly = true)
class AppleService(
    val appleConfig : AppleConfig,
    val appleJwtProvider : AppleJwtProvider,
    val appleAuthService: AppleAuthService,
    val networkService: NetworkService,
    val memberQueryService : MemberQueryService,
    val memberService : MemberService,
    val jwtTokenProvider : JwtTokenProvider,
    val idGenerator: IdGenerator,
    val time : Time
) {
    val appleEndpointUrl = "https://appleid.apple.com/auth/token"

    @Transactional
    fun loginAppleWithAdd(
        request : AppleLoginServiceRequest
    ) : Member{

        val appleJwtToken = getAppleJwtToken(request)

        val appleProfile = appleAuthService.extractAppleProfileWithValidate(appleJwtToken)

        var findMember = memberQueryService.findMemberFromProviderId(appleProfile.appleId)

        if(isNull(findMember)){
            validateEmail(appleProfile.email)

            findMember = appleSignup(appleProfile)
        }


        return findMember!!
    }

    private fun getAppleJwtToken(request: AppleLoginServiceRequest): String {
        val response = getApplePostResponse(request)

        return response["id_token"] as? String ?: throw GlobalException(OAuth2ErrorCode.APPLE_JWT_NOT_FOUND)
    }

    private fun getApplePostResponse(request: AppleLoginServiceRequest): Map<String, Any> {
        val clientSecret = appleJwtProvider.createClientSecret()

        val formData: MultiValueMap<String, String> = getFormData(clientSecret, request)

        return postOauth2Apple(formData)
    }

    private fun getFormData(
        clientSecret: String,
        request: AppleLoginServiceRequest
    ): MultiValueMap<String, String> {
        val formData: MultiValueMap<String, String> = LinkedMultiValueMap()
        formData.add("client_id", appleConfig.clientId)
        formData.add("client_secret", clientSecret)
        formData.add("code", request.appleAuthCode)
        formData.add("grant_type", "authorization_code")
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
        return memberService.addMemberByOAuth2(member)
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