package kr.co.fitview.api.app.global.config

import io.jsonwebtoken.security.Keys

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import java.util.*
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec


@Configuration
class JwtConfig(
    @Value("\${jwt.secret}")
    val secretKeyString: String,

    @Value("\${jwt.access-token-validity-in-ms}")
    val accessTokenValidityInMs: Long,

    @Value("\${jwt.refresh-token-validity-in-ms}")
    val refreshTokenValidityInMs: Long,

    ) {

    final val algorithm = "HmacSHA256"
    final val secretKey: SecretKey
    final val secretKeySpec: SecretKeySpec

//    @PostConstruct
//    fun init() {
//        val keyBytes = Base64.getDecoder().decode(secretKeyString)
//        if (keyBytes.size < 32) {
//            throw IllegalArgumentException("jwt 비밀키는 32바이트 이상이어야 합니다.")
//        }
//        this.secretKey = Keys.hmacShaKeyFor(keyBytes)
//        this.secretKeySpec = SecretKeySpec(keyBytes, algorithm)
//    }

    init {
        val keyBytes = Base64.getDecoder().decode(secretKeyString)

        validateKeyBytes(keyBytes)
        validateValidityInMs()

        this.secretKey = Keys.hmacShaKeyFor(keyBytes)
        this.secretKeySpec = SecretKeySpec(keyBytes, algorithm)

    }

    private fun validateKeyBytes(keyBytes: ByteArray) {
        if (keyBytes.size < 32) {
            throw IllegalArgumentException("jwt 비밀키는 32바이트 이상이어야 합니다.")
        }
    }

    private fun validateValidityInMs() {
        if (accessTokenValidityInMs <= 0L || refreshTokenValidityInMs <= 0L) {
            throw IllegalArgumentException("jwt 유효기간은 양수여야 합니다.")
        }
    }

}
