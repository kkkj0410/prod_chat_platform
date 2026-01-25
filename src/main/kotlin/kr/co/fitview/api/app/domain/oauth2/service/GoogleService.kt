package kr.co.fitview.api.app.domain.oauth2.service

import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.domain.member.service.MemberService
import kr.co.fitview.api.app.domain.oauth2.config.GoogleConfig
import kr.co.fitview.api.app.domain.oauth2.dto.request.GoogleLoginServiceRequest
import kr.co.fitview.api.app.domain.oauth2.dto.response.GoogleProfile
import kr.co.fitview.api.app.global.entity.OAuth2Provider
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.oauth2.OAuth2ErrorCode
import kr.co.fitview.api.app.global.id.IdGenerator
import kr.co.fitview.api.app.global.network.NetworkService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

@Service
@Transactional(readOnly = true)
class GoogleService(
    private val networkService : NetworkService,
    private val memberQueryService : MemberQueryService,
    private val memberService : MemberService,
    private val googleConfig: GoogleConfig,
    private val idGenerator: IdGenerator
) {


    @Transactional
    fun loginGoogleWithAdd(request : GoogleLoginServiceRequest): Member {

        val googleAccessToken = getGoogleAccessToken(request.googleAuthCode)

        val googleProfile = getGoogleProfile(googleAccessToken)

        var findMember = memberQueryService.findMemberFromProviderId(googleProfile.googleId)

        if (isNull(findMember)) {
            findMember = googleSignup(googleProfile)
        }

        return findMember!!
    }

    private fun getGoogleAccessToken(authCode: String): String {
        val formData = getFormData(authCode)

        val response = getGoogleAccessTokenResponse(formData)

        return getAccessTokenByResponse(response)
    }

    private fun getFormData(authCode: String): MultiValueMap<String, String> {

        val formData: MultiValueMap<String, String> = LinkedMultiValueMap()
        formData.add("code", authCode)
        formData.add("client_id", googleConfig.clientIdWeb)
        formData.add("client_secret", googleConfig.clientSecretWeb)
        formData.add("redirect_uri", googleConfig.redirectUriWeb)
        formData.add("grant_type", "authorization_code")

        return formData
    }

    private fun getGoogleAccessTokenResponse(formData: MultiValueMap<String, String>): Map<String, Any> {
        try {
            return networkService.postByWebClient(formData, googleConfig.googleAccessTokenUrl)
        } catch (e: Exception) {
            throw GlobalException(OAuth2ErrorCode.GOOGLE_AUTH_CODE_EXCHANGE_FAILED)
        }
    }

    private fun getAccessTokenByResponse(response: Map<String, Any>) =
        (response["access_token"] as? String
        ?: throw GlobalException(OAuth2ErrorCode.GOOGLE_ACCESS_TOKEN_PARSE_FAILED))

    private fun getGoogleProfile(accessToken: String): GoogleProfile {
        val accessTokenHeader = createAccessTokenHeader(accessToken)

        val response = getGoogleProfileResponse(accessTokenHeader)

        return getGoogleProfileByResponse(response)
    }

    private fun createAccessTokenHeader(accessToken: String): Map<String, String> {
        val headers = mapOf("Authorization" to "Bearer $accessToken")
        return headers
    }

    private fun getGoogleProfileResponse(accessTokenHeader: Map<String, String>): Map<String, Any> {
        val response = try {
            networkService.getByWebClient(googleConfig.googleProfileUrl, accessTokenHeader)
        } catch (e: Exception) {
            throw GlobalException(OAuth2ErrorCode.GOOGLE_PROFILE_REQUEST_FAILED)
        }
        return response
    }

    private fun getGoogleProfileByResponse(response: Map<String, Any>): GoogleProfile {
        return GoogleProfile(
            googleId = response["id"] as? String ?: throw GlobalException(OAuth2ErrorCode.GOOGLE_PROFILE_ID_MISSING),
            email = response["email"] as? String ?: throw GlobalException(OAuth2ErrorCode.GOOGLE_PROFILE_EMAIL_MISSING)
        )
    }

    private fun googleSignup(profile: GoogleProfile): Member {
        val member = createGoogleMember(profile.googleId, profile.email)
        return memberService.addMemberByOAuth2(member)
    }

    private fun createGoogleMember(providerId: String, email: String) = Member(
        provider = OAuth2Provider.GOOGLE,
        providerId = providerId,
        email = email,
        password = idGenerator.createUuid(),
        role = Role.USER
    )

    private fun <T> isNull(obj: T?) = obj == null
}
