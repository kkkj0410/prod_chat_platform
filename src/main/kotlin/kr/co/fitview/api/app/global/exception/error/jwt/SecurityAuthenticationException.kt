package kr.co.fitview.api.app.global.exception.error.jwt

import kr.co.fitview.api.app.global.exception.error.ErrorCode
import org.springframework.security.core.AuthenticationException

class SecurityAuthenticationException (
    val errorCode: ErrorCode
) : AuthenticationException(errorCode.message)
