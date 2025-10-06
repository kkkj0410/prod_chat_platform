package kr.co.fitview.api.app.global.util

import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.jwt.JwtErrorCode
import kr.co.fitview.api.app.global.security.UserPrincipal
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class SecurityUtil {


    fun getMemberId(): Long {
        val authentication = SecurityContextHolder.getContext().authentication
        validateAuthentication(authentication)

        val userPrincipal: UserPrincipal = getUserPrincipalFrom(authentication)

        return userPrincipal.memberId
    }

    private fun validateAuthentication(authentication: Authentication?) {
        if (isNull(authentication) || isNotAuthenticated(authentication!!) || isNotUserPrincipal(authentication)) {
            throw GlobalException(JwtErrorCode.JWT_TOKEN_INVALID)
        }
    }

    private fun isNull(authentication: Authentication?) = authentication == null

    private fun isNotAuthenticated(authentication: Authentication) = !authentication.isAuthenticated

    private fun isNotUserPrincipal(authentication: Authentication) =
        authentication.principal !is UserPrincipal

    private fun getUserPrincipalFrom(authentication: Authentication) =
        authentication.principal as UserPrincipal

}