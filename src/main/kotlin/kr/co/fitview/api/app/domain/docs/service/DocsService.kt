package kr.co.fitview.api.app.domain.docs.service

import kr.co.fitview.api.app.domain.docs.dto.request.DocsLoginServiceRequest
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.service.MemberService
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.auth.AuthErrorCode
import kr.co.fitview.api.app.global.exception.error.member.MemberErrorCode
import kr.co.fitview.api.app.global.jwt.JwtTokenProvider
import org.springframework.http.HttpHeaders
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service


@Service
class DocsService(
    val jwtTokenProvider: JwtTokenProvider,
    val memberService : MemberService,
    val passwordEncoder : PasswordEncoder
) {

    fun login(request: DocsLoginServiceRequest): HttpHeaders {
        val findMember = findMember(request.email, request.password)

        val docsToken = jwtTokenProvider.createDocsToken(findMember.id!!, findMember.role!!)

        return convertCookieHeader(docsToken)
    }

    private fun findMember(loginId : String, password : String) : Member {
        val member = findMemberElseThrow(loginId)

        validatePassword(password, member.password!!)

        return member
    }

    private fun findMemberElseThrow(email : String): Member {
        return (memberService.findMemberFromEmail(email)
            ?: throw GlobalException(MemberErrorCode.MEMBER_NOT_FOUND))
    }

    private fun validatePassword(
        inputPassword: String,
        encodedPassword: String
    ) {
        if (isNotMatchPassword(inputPassword, encodedPassword)) {
            throw GlobalException(AuthErrorCode.INVALID_PASSWORD)
        }
    }

    private fun isNotMatchPassword(inputPassword: String, encodedPassword: String) =
        !passwordEncoder.matches(inputPassword, encodedPassword)

    private fun convertCookieHeader(accessToken: String): HttpHeaders {
        val accessTokenCookie = jwtTokenProvider.convertRestrictCookieFromAccessToken(accessToken)

        val accessTokenCookieHeader = convertHeader(accessTokenCookie.toString())

        return accessTokenCookieHeader
    }

    private fun convertHeader(accessTokenCookie: String) : HttpHeaders {
        return HttpHeaders()
            .apply { add(HttpHeaders.SET_COOKIE, accessTokenCookie) }
    }

}