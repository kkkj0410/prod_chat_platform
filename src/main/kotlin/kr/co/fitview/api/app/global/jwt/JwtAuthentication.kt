package kr.co.fitview.api.app.global.jwt

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Component


@Component
class JwtAuthentication(
    val jwtTokenProvider : JwtTokenProvider
) {


    fun setAuthentication(){

//        UsernamePasswordAuthenticationToken()
    }

    fun getUserPrincipalFrom(accessToken : String){
        jwtTokenProvider.extractMemberIdFrom(accessToken)
        jwtTokenProvider.extractRoleFrom(accessToken)


    }


}