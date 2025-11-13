//package kr.co.fitview.api.app.domain.chat.config
//
//import org.springframework.messaging.support.NativeMessageHeaderAccessor
//import java.security.Principal
//
//
//
//class SimpMessageHeaderAccessor : NativeMessageHeaderAccessor() {
//
//    fun setUser(principal: Principal) {
//        this.setHeader("simpUser", principal)
//        if (this.userCallback != null) {
//            this.userCallback.accept(principal)
//        }
//    }
//}