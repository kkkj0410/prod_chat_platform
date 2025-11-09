package kr.co.fitview.api.app.domain.auth.service

import kr.co.fitview.api.app.domain.auth.HeaderClientType
import kr.co.fitview.api.app.domain.auth.dto.request.AccessTokenRefreshServiceRequest
import kr.co.fitview.api.app.domain.auth.dto.request.AuthSignupRequestTest
import kr.co.fitview.api.app.domain.auth.dto.response.MemberLoginResponse
import kr.co.fitview.api.app.domain.auth.dto.response.AccessTokenRefreshResponse
import kr.co.fitview.api.app.domain.member.dto.request.MemberCreateServiceRequest
import kr.co.fitview.api.app.domain.member.dto.request.MemberLoginServiceRequest
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.service.MemberService
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.auth.AuthErrorCode
import kr.co.fitview.api.app.global.exception.error.member.MemberErrorCode
import kr.co.fitview.api.app.global.jwt.JwtTokenProvider
import org.springframework.http.HttpHeaders
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class AuthService(
    val memberService : MemberService,
    val passwordEncoder : PasswordEncoder,
    val jwtTokenProvider : JwtTokenProvider,
    val refreshTokenService: RefreshTokenService
) {

    @Transactional
    fun signup(request : MemberCreateServiceRequest) : Member{
        val encryptedPassword = passwordEncoder.encode(request.password)
        val member = createMember(request, encryptedPassword)

        return memberService.addMember(member)
    }

    @Transactional
    fun login(
        request: MemberLoginServiceRequest,
        headerClientType : HeaderClientType
    ): MemberLoginResponse {
        val findMember = findMember(request)

        val accessToken = jwtTokenProvider.createAccessToken(findMember.id!!, findMember.role!!)
        val refreshToken = refreshTokenService.issueRefreshToken(findMember.id!!)

        if(isMobile(headerClientType)){
            return MemberLoginResponse(accessToken, refreshToken, null)
        }

        val refreshTokenCookieHeader = convertCookieHeader(refreshToken)
        return MemberLoginResponse(accessToken, null, refreshTokenCookieHeader)
    }


    @Transactional
    fun refreshAccessToken(request: AccessTokenRefreshServiceRequest): AccessTokenRefreshResponse {
        refreshTokenService.validateRefreshTokenFrom(request.refreshToken)

        val memberId = jwtTokenProvider.extractMemberIdFrom(request.refreshToken)
        val findMember = memberService.findMemberOrElseThrow(memberId)

        val accessToken = jwtTokenProvider.createAccessToken(findMember.id!!, findMember.role!!)

        return AccessTokenRefreshResponse(accessToken)
    }

    private fun createMember(
        request: MemberCreateServiceRequest,
        encryptedPassword: String
    ) = Member(
        email = request.email,
        password = encryptedPassword,
        role = Role.USER
    )

    private fun findMember(request: MemberLoginServiceRequest) : Member {
        val member = findMemberElseThrow(request)

        validatePassword(request.password, member.password!!)

        return member
    }

    private fun findMemberElseThrow(request: MemberLoginServiceRequest): Member {
        return (memberService.findMemberFromLoginId(request.email)
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

    private fun isMobile(headerClientType: HeaderClientType) =
        headerClientType == HeaderClientType.MOBILE

    private fun convertCookieHeader(refreshToken: String): HttpHeaders {
        val refreshTokenCookie = jwtTokenProvider.convertRestrictCookieFromRefreshToken(refreshToken)

        val refreshTokenCookieHeader = convertHeader(refreshTokenCookie.toString())

        return refreshTokenCookieHeader
    }

    private fun convertHeader(refreshTokenCookie: String) : HttpHeaders {
        return HttpHeaders().apply { add(HttpHeaders.SET_COOKIE, refreshTokenCookie) }
    }



}