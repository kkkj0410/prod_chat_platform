package kr.co.fitview.api.app.global.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import kr.co.fitview.api.app.global.config.JwtConfig
import kr.co.fitview.api.app.global.constant.JwtConstant
import kr.co.fitview.api.app.global.cookie.CookieProvider
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.jwt.JwtErrorCode
import kr.co.fitview.api.app.global.exception.error.jwt.SecurityAuthenticationException
import kr.co.fitview.api.app.global.id.IdGenerator
import kr.co.fitview.api.app.global.time.Time
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component



@Component
class JwtTokenProvider(
    val jwtConfig : JwtConfig,
    val cookieProvider : CookieProvider,
    val idGenerator : IdGenerator,
    val time : Time
) {

    fun createAccessToken(
        memberId: Long, role: Role
    ): String {

        return Jwts.builder()
            .header()
            .add(JwtConstant.JWT_HEADER_TYPE, JwtConstant.JWT_HEADER_TYPE_VALUE)
            .and()
            .subject(memberId.toString())
            .claim(JwtConstant.ROLE, role)
            .claim(JwtConstant.TYP, JwtConstant.TYP_ACCESS)
            .issuedAt(time.nowDate)
            .expiration(time.nowDatePlus(jwtConfig.accessTokenValidityInMs))
            .signWith(jwtConfig.secretKeySpec)
            .compact()
    }

    // API 문서 로그인을 위한 함수. createAccessToken에서 만료시간만 다름
    fun createDocsToken(
        memberId: Long, role: Role
    ): String {

        return Jwts.builder()
            .header()
            .add(JwtConstant.JWT_HEADER_TYPE, JwtConstant.JWT_HEADER_TYPE_VALUE)
            .and()
            .subject(memberId.toString())
            .claim(JwtConstant.ROLE, role)
            .claim(JwtConstant.TYP, JwtConstant.TYP_ACCESS)
            .issuedAt(time.nowDate)
            .expiration(time.nowDatePlus(jwtConfig.docsTokenValidityInMs))
            .signWith(jwtConfig.secretKeySpec)
            .compact()
    }

    fun createRefreshToken(
        memberId : Long
    ) : String {
        return Jwts.builder()
            .header()
            .add(JwtConstant.JWT_HEADER_TYPE, JwtConstant.JWT_HEADER_TYPE_VALUE)
            .and()
            .subject(memberId.toString())
            .claim(JwtConstant.CLAIM_JTI, idGenerator.createUuid())
            .claim(JwtConstant.TYP, JwtConstant.TYP_REFRESH)
            .issuedAt(time.nowDate)
            .expiration(time.nowDatePlus(jwtConfig.refreshTokenValidityInMs))
            .signWith(jwtConfig.secretKeySpec)
            .compact()
    }

    fun convertRestrictCookieFromRefreshToken(
        refreshToken : String
    ) : ResponseCookie {
        return cookieProvider.createRestrictCookie(
            JwtConstant.REFRESH_TOKEN_COOKIE_NAME,
            refreshToken,
            jwtConfig.refreshTokenValidityInMs
        )
    }



    fun extractMemberIdFrom(jwtToken : String): Long{
        val claims = extractClaimsFrom(jwtToken)
        return claims.subject.toLong()
    }

    fun extractRoleFrom(accessToken : String) : Role{
        val claims = extractClaimsFrom(accessToken)
        return Role.from(claims[JwtConstant.ROLE] as String)
    }

    fun extractUuidFrom(refreshToken : String) : String{
        val claims = extractClaimsFrom(refreshToken)
        return claims[JwtConstant.CLAIM_JTI] as String
    }


    private fun extractClaimsFrom(jwtToken: String): Claims {
        try{
            return Jwts.parser()
                .verifyWith(jwtConfig.secretKey)
                .build()
                .parseSignedClaims(jwtToken)
                .payload
        }catch(ex : ExpiredJwtException){
            throw GlobalException(JwtErrorCode.JWT_TOKEN_EXPIRED)
        }catch(ex : JwtException){
            throw GlobalException(JwtErrorCode.JWT_TOKEN_INVALID)
        }
    }

//    private fun validateExpired(jwtToken : String){
//        val claims = extractClaimsFrom(jwtToken)
//        val expiration = claims.expiration
//        if (expiration.before(time.nowDate)) {
//            throw GlobalException(JwtErrorCode.JWT_TOKEN_EXPIRED)
//        }
//    }


    // 해당 함수는 API 문서를 위한 임시 함수
    // 실제 서비스에서 사용X
    fun convertRestrictCookieFromAccessToken(
        accessToken : String
    ) : ResponseCookie {
        return cookieProvider.createRestrictCookie(
            JwtConstant.DOCS_TOKEN_COOKIE_NAME,
            accessToken,
            jwtConfig.docsTokenValidityInMs
        )
    }

}