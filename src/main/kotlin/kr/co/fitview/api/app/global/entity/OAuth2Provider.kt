package kr.co.fitview.api.app.global.entity

enum class OAuth2Provider {
    APPLE, KAKAO;

//    companion object {
//        fun from(value: String): OAuth2Provider {
//            return OAuth2Provider.entries.find { it.name == value }
//                ?: throw IllegalArgumentException("매칭되는 플랫폼이 없습니다.")
//        }
//    }
}