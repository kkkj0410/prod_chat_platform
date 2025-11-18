//package kr.co.fitview.api.app.domain.chat.config
//
//import org.springframework.http.server.ServerHttpRequest
//import org.springframework.http.server.ServerHttpResponse
//import org.springframework.stereotype.Component
//import org.springframework.web.socket.WebSocketHandler
//import org.springframework.web.socket.server.HandshakeInterceptor
//import java.security.Principal
//import java.util.*
//
//@Component
//class UserHandshakeInterceptor : HandshakeInterceptor {
//
//    override fun beforeHandshake(
//        request: ServerHttpRequest,
//        response: ServerHttpResponse,
//        wsHandler: WebSocketHandler,
//        attributes: MutableMap<String, Any>
//    ): Boolean {
//        // 예: query param에서 userId 가져오기
//        val uri = request.uri
//        val query = uri.query ?: ""
//        val userId = query.split("&")
//            .firstOrNull { it.startsWith("userId=") }
//            ?.split("=")?.get(1) ?: UUID.randomUUID().toString()
//
//        // WebSocket 세션에 principal 등록
//        attributes["principal"] = object : Principal {
//            override fun getName(): String = userId
//        }
//
//        return true
//    }
//
//    override fun afterHandshake(
//        request: ServerHttpRequest,
//        response: ServerHttpResponse,
//        wsHandler: WebSocketHandler,
//        exception: Exception?
//    ) {
//        // 필요 없음
//    }
//}