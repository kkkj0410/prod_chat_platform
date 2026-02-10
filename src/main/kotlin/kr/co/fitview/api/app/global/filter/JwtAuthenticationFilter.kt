package kr.co.fitview.api.app.global.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kr.co.fitview.api.app.domain.auth.constant.AuthConstant
import kr.co.fitview.api.app.domain.stat.service.ActiveMemberStatService
import kr.co.fitview.api.app.global.constant.JwtConstant
import kr.co.fitview.api.app.global.constant.SecurityConstant
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.jwt.JwtErrorCode
import kr.co.fitview.api.app.global.jwt.JwtAuthentication
import kr.co.fitview.api.app.global.jwt.JwtTokenProvider
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter


@Component
class JwtAuthenticationFilter(
    val jwtAuthentication : JwtAuthentication,
    val antPathMatcher: AntPathMatcher,
    val activeMemberStatService : ActiveMemberStatService,
    val jwtTokenProvider : JwtTokenProvider
)  : OncePerRequestFilter(){


    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val uri = request.requestURI

        if(isDocsPath(uri)){
            val docsAccessToken = getDocsAccessTokenElseThrow(request)
            jwtAuthentication.setAuthentication(docsAccessToken)

            filterChain.doFilter(request, response)
            return
        }

        val findAccessToken = getAccessToken(request)

        if (isSkipFilter(findAccessToken, uri)) {
            filterChain.doFilter(request, response)
            return
        }

        if (isSkipFilterApplyTokenIfValid(findAccessToken, uri)) {
            try {
                jwtAuthentication.setAuthentication(findAccessToken!!)
                saveActiveMemberStat(findAccessToken)
                filterChain.doFilter(request, response)
                return
            } catch (e: Exception) {
                filterChain.doFilter(request, response)
                return
            }
        }

        if(hasToken(findAccessToken)){
            jwtAuthentication.setAuthentication(findAccessToken!!)
            saveActiveMemberStat(findAccessToken)
        }
        filterChain.doFilter(request, response)
    }

    private fun isDocsPath(uri : String) : Boolean {
        return SecurityConstant.DOCS_URIS.any { pattern -> antPathMatcher.match(pattern, uri) }
    }

    private fun getDocsAccessTokenElseThrow(request: HttpServletRequest) = (getDocsAccessTokenFromCookie(request)
        ?: throw GlobalException(JwtErrorCode.JWT_TOKEN_MISSING))

    private fun getAccessToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader(AuthConstant.AUTH_HEADER)
        if (hasToken(bearerToken) && bearerToken.startsWith(AuthConstant.TOKEN_PREFIX)) {
            return bearerToken.substring(AuthConstant.TOKEN_PREFIX_LENGTH)
        }
        return null
    }

    private fun isSkipFilter(findAccessToken: String?, uri: String) =
        hasNotToken(findAccessToken) && isPermitAllPath(uri)

    private fun isSkipFilterApplyTokenIfValid(findAccessToken: String?, uri: String) = hasToken(findAccessToken) && isPermitAllPath(uri)

    private fun getDocsAccessTokenFromCookie(request : HttpServletRequest) : String? {
        val cookies = request.cookies

        return cookies
            .firstOrNull { it.name == JwtConstant.DOCS_TOKEN_COOKIE_NAME }
            ?.value
    }

    private fun isPermitAllPath(uri: String): Boolean {
        return SecurityConstant.PERMIT_ALL_URIS.any { pattern ->
            antPathMatcher.match(pattern, uri)
        }
    }

    private fun hasToken(token: String?): Boolean = token != null

    private fun hasNotToken(token: String?) = !hasToken(token)

    private fun saveActiveMemberStat(accessToken : String){
        val memberId = jwtTokenProvider.extractMemberIdFrom(accessToken)
        activeMemberStatService.saveActiveMemberStat(memberId)
    }

}