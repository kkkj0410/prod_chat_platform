package kr.co.fitview.api.app.domain.oauth2.util

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import kr.co.fitview.api.app.domain.oauth2.config.AppleConfig
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.oauth2.OAuth2ErrorCode
import kr.co.fitview.api.app.global.time.Time
import org.springframework.stereotype.Component
import java.security.KeyFactory
import java.security.interfaces.ECPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*


@Component
class AppleJwtProvider(
    val appleConfig : AppleConfig,
    val time : Time
) {

    fun createClientSecret(): String {
        val applePrivateKey = extractApplePrivateKey()

        val signKey: Algorithm = createSignKey(applePrivateKey)

        return createJwt(signKey)
    }


    private fun extractApplePrivateKey() : String {
        val header = "-----BEGIN PRIVATE KEY-----"
        val footer = "-----END PRIVATE KEY-----"

        validateAppleKeyFormat(header, footer)

        return appleConfig.key
            .replace(header, "")
            .replace(footer, "")
            .replace("\\s+".toRegex(), "")
    }

    private fun validateAppleKeyFormat(header : String, footer : String) {
        if (isNotContain(header) || isNotContain(footer)) {
            throw GlobalException(OAuth2ErrorCode.APPLE_KEY_INVALID_FORMAT)
        }
    }

    private fun isNotContain(header: String) = !appleConfig.key.contains(header)


    private fun createSignKey(applePrivateKey: String): Algorithm {
        try{
            val pkcs8EncodedBytes = Base64.getDecoder().decode(applePrivateKey)
            val keySpec = PKCS8EncodedKeySpec(pkcs8EncodedBytes)
            val keyFactory = KeyFactory.getInstance("EC")
            val privateKey = keyFactory.generatePrivate(keySpec)
            val signKey: Algorithm = Algorithm.ECDSA256(null, privateKey as ECPrivateKey)
            return signKey
        }catch(ex : Exception){
            throw GlobalException(OAuth2ErrorCode.APPLE_SIGN_KEY_GENERATION_FAILED)
        }

    }

    private fun createJwt(signKey: Algorithm): String = JWT.create()
        .withKeyId(appleConfig.keyId)
        .withIssuer(appleConfig.teamId)
        .withIssuedAt(time.nowDate)
        .withExpiresAt(time.nowDatePlus(appleConfig.jwtValidityInMs))
        .withAudience(appleConfig.appleAuthServer)
        .withSubject(appleConfig.clientId)
        .sign(signKey)

}