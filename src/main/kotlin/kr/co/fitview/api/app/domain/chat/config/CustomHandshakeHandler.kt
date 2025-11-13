package kr.co.fitview.api.app.domain.chat.config

import jakarta.servlet.http.HttpServletRequest
import kr.co.fitview.api.app.global.jwt.JwtTokenProvider
import kr.co.fitview.api.app.global.security.UserPrincipal
import org.springframework.http.server.ServerHttpRequest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.support.DefaultHandshakeHandler
import java.security.Principal
import java.util.*

@Component
class CustomHandshakeHandler(
    private val jwtTokenProvider: JwtTokenProvider
) : DefaultHandshakeHandler() {


    override fun determineUser(
        request: ServerHttpRequest,
        wsHandler: WebSocketHandler,
        attributes: MutableMap<String, Any>
    ): Principal {
        // query parameter에서 userId 가져오기

        // Authorization 헤더에서 JWT 가져오기
//        val bearerToken = request.headers.getFirst("Authorization")
//            ?: throw IllegalStateException("JWT 토큰 없음")
//        val token = bearerToken.removePrefix("Bearer ")
//
//
//        return StompPrincipal(jwtTokenProvider.extractMemberIdFrom(token))

        return StompPrincipal(UUID.randomUUID().toString())
    }

    private fun createUserPrincipalFrom(accessToken : String) : UserPrincipal {
        val memberId = jwtTokenProvider.extractMemberIdFrom(accessToken)
        val role = jwtTokenProvider.extractRoleFrom(accessToken)

        return UserPrincipal(memberId, role)
    }


}