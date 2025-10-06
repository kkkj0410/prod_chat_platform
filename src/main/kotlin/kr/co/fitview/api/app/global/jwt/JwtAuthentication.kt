package kr.co.fitview.api.app.global.jwt

import kr.co.fitview.api.app.global.security.UserPrincipal
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component


@Component
class JwtAuthentication(
    val jwtTokenProvider : JwtTokenProvider
) {


    fun setAuthentication(accessToken : String){
        val userPrincipal = createUserPrincipalFrom(accessToken)

        val authentication = createAuthentication(userPrincipal)

        SecurityContextHolder.getContext().authentication = authentication
    }

    private fun createUserPrincipalFrom(accessToken : String) : UserPrincipal{
        val memberId = jwtTokenProvider.extractMemberIdFrom(accessToken)
        val role = jwtTokenProvider.extractRoleFrom(accessToken)

        return UserPrincipal(memberId, role)
    }

    private fun createAuthentication(userPrincipal: UserPrincipal): UsernamePasswordAuthenticationToken {
        return UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.authorities)
    }


}