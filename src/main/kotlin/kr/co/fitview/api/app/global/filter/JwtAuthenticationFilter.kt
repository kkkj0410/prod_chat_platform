package kr.co.fitview.api.app.global.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kr.co.fitview.api.app.global.constant.JwtConstant
import kr.co.fitview.api.app.global.constant.SecurityConstant
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter


@Component
class JwtAuthenticationFilter(
    val pathMatcher: AntPathMatcher = AntPathMatcher()
)  : OncePerRequestFilter(){



    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val uri = request.requestURI

        // api 문서 로그인 uri
        if(isDocsPath(uri)){
            val docsAccessToken = getDocsAccessTokenFromCookie(request)


        }

        //

        filterChain.doFilter(request, response)
    }

    private fun isDocsPath(uri : String) : Boolean {
        return SecurityConstant.DOCS_URIS.any { pattern -> pathMatcher.match(pattern, uri) }
    }

    private fun getDocsAccessTokenFromCookie(request : HttpServletRequest) : String? {
        val cookies = request.cookies

        return cookies
            .firstOrNull { it.name == JwtConstant.DOCS_TOKEN_COOKIE_NAME }
            ?.value
    }



//
//    // swagger 전용 로그인 검증
//    String swaggerToken = getSwaggerTokenFromCookie(request);
//    if(isSwaggerPath(uri) && swaggerToken != null){
//        try{
//            jwtTokenProvider.validateAccessToken(swaggerToken);
//            Claims accessTokenClaims = jwtTokenProvider.getClaims(swaggerToken);
//            jwtAuthentication.setAuthentication(accessTokenClaims);
//            filterChain.doFilter(request, response);
//            return;
//        }catch (AccessTokenUnavailableException e) {
//            log.warn("[SwaggerToken] {}: {} (URI: {}, IP: {})", e.getMessage(), swaggerToken, uri, ip);
//            throw e;
//        }
//    }
//
//
//    //swagger 전용 로그인이 아닐 시, 기존대로 권한 체크
//    String accessToken = getAccessToken(request);
//
//    //1. accessToken 없고, 모두 서비스 제공 가능한 URI 이면 사용 허가
//    if(accessToken == null && isPermitAllPath(uri)){
//        filterChain.doFilter(request, response);
//        return;
//    }
//
//    //2. accessToken 존재하지만 무효이면서, 모두 서비스 제공 가능한 URI 이면 사용 허가 (대신 유효한 토큰이면 사용자 정보도 내부에서 사용)
//    if(accessToken != null && isPermitAllPath(uri)){
//        try{
//            jwtTokenProvider.validateAccessToken(accessToken);
//            Claims accessTokenClaims = jwtTokenProvider.getClaims(accessToken);
//            jwtAuthentication.setAuthentication(accessTokenClaims);
//            filterChain.doFilter(request, response);
//            return;
//        }catch(AccessTokenUnavailableException e){
//            filterChain.doFilter(request, response);
//            return;
//        }
//    }
//
//    //3. 사용자 권한이 필요한 API 사용 시, 검증 통과한 후 서비스 제공
//    jwtTokenProvider.validateAccessToken(accessToken);
//    Claims accessTokenClaims = jwtTokenProvider.getClaims(accessToken);
//    jwtAuthentication.setAuthentication(accessTokenClaims);
//    filterChain.doFilter(request, response);
//}
//
//private boolean isPermitAllPath(String uri) {
//    return SecurityConstants.PERMIT_ALL_URIS.stream()
//        .anyMatch(pattern -> pathMatcher.match(pattern, uri));
//}
//
//private boolean isSwaggerPath(String uri) {
//    return SecurityConstants.SWAGGER_URI.stream()
//        .anyMatch(pattern -> pathMatcher.match(pattern, uri));
//}
//
//
//private String getAccessToken(HttpServletRequest request) {
//    String bearerToken = request.getHeader(AuthConstants.AUTH_HEADER);
//    if (bearerToken != null && bearerToken.startsWith(AuthConstants.TOKEN_PREFIX)) {
//        return bearerToken.substring(AuthConstants.TOKEN_PREFIX_LENGTH);
//    }
//    return null;
//}
//
//private String getSwaggerTokenFromCookie(HttpServletRequest request) {
//    Cookie[] cookies = request.getCookies();
//    if (cookies != null) {
//        return Arrays.stream(cookies)
//            .filter(cookie -> JwtConstants.SWAGGER_TOKEN_COOKIE_NAME.equals(cookie.getName()))
//        .map(Cookie::getValue)
//            .findFirst()
//            .orElse(null);
//    }
//    return null;
//}
}