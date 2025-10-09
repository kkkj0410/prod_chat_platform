package kr.co.fitview.api.app.domain.oauth2.service

import com.nimbusds.jose.JWSVerifier
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.oauth2.config.AppleConfig
import kr.co.fitview.api.app.domain.oauth2.dto.request.AppleLoginRequest
import kr.co.fitview.api.app.domain.oauth2.dto.response.AppleProfile
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.oauth2.OAuth2ErrorCode
import kr.co.fitview.api.app.global.time.Time
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import java.net.URI


@Service
class AppleAuthService(
    val appleConfig: AppleConfig,
    val time : Time
) {

    val applePublicEndPointUrl = "https://appleid.apple.com/auth/keys"
    val appleIssuerUrl = "https://appleid.apple.com"


    fun extractAppleProfileWithValidate(
        appleJwtToken : String
    ) : AppleProfile {

        val signedJwt = SignedJWT.parse(appleJwtToken)

        val validator = createAppleJwtVerifier(signedJwt)

        validateAppleJwt(signedJwt, validator)

        val claims = getClaimsFrom(signedJwt)

        validateIssuer(claims)

        validateAudience(claims)

        validateTime(claims)

        val appleId = claims.subject
        val email: String? = extractEmailFrom(claims)

        return AppleProfile(appleId, email)
    }


    private fun createAppleJwtVerifier(signedJwt: SignedJWT) : JWSVerifier {
        // 서명 키
        val targetKid = signedJwt.header.keyID

        // 애플 퍼블릭 키 모음
        val appleJwkSet = JWKSet.load(URI(applePublicEndPointUrl).toURL())

        // 타겟 서명 키와 일치하는 퍼블릭 키 찾기
        val appleJwk = appleJwkSet.getKeyByKeyId(targetKid)

        // 퍼블릭 키 -> RSA 키 변환
        val appleRsaKey = appleJwk as? RSAKey ?: throw GlobalException(OAuth2ErrorCode.APPLE_JWT_CONVERTER_FAIL)

        // RSA 키 -> RSA 퍼블릭 키 변환
        val appleRsaPublicKey = appleRsaKey.toRSAPublicKey()

        // 검증 대상 키에 대한 인증 객체 생성
        return RSASSAVerifier(appleRsaPublicKey)
    }



    private fun getClaimsFrom(signedJwt: SignedJWT): JWTClaimsSet =
        signedJwt.jwtClaimsSet


    private fun validateAppleJwt(signedJwt: SignedJWT, validator: JWSVerifier) {
        if (isNotVerify(signedJwt, validator)) {
            throw GlobalException(OAuth2ErrorCode.APPLE_SIGNED_INVALID)
        }
    }

    private fun isNotVerify(signedJwt: SignedJWT, validator: JWSVerifier) =
        !signedJwt.verify(validator)

    private fun validateIssuer(claims: JWTClaimsSet) {
        if (isNotMatchIssuer(claims)) {
            throw GlobalException(OAuth2ErrorCode.APPLE_ISSUER_INVALID)
        }
    }

    private fun isNotMatchIssuer(claims: JWTClaimsSet) = claims.issuer != appleIssuerUrl

    private fun validateAudience(claims: JWTClaimsSet) {
        val audience = claims.audience
        if (isNotContains(audience)) {
            throw GlobalException(OAuth2ErrorCode.APPLE_AUDIENCE_INVALID)
        }
    }

    private fun isNotContains(audience: List<String>): Boolean {
        return !audience.any { it == appleConfig.clientId || it == appleConfig.bundleId }
    }


    private fun validateTime(claims: JWTClaimsSet) {
        if (isExpired(claims)) {
            throw GlobalException(OAuth2ErrorCode.APPLE_JWT_EXPIRED)
        }
    }

    private fun isExpired(claims: JWTClaimsSet) = time.nowDate.after(claims.expirationTime)

    private fun extractEmailFrom(claims: JWTClaimsSet): String? {
        val email: String? = claims.getStringClaim("email")
        return email
    }

}