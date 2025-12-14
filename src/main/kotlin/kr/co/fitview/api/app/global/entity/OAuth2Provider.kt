package kr.co.fitview.api.app.global.entity

import com.fasterxml.jackson.annotation.JsonCreator

enum class OAuth2Provider(val description : String) {
    APPLE("애플"),
    KAKAO("카카오"),
    GOOGLE("구글")

    ;

    override fun toString(): String {
        return "$name: $description"
    }

    companion object {
        fun allDescription(): List<String> {
            return entries.map { it.toString() }
        }
    }

//    companion object {
//        fun from(value: String): OAuth2Provider {
//            return OAuth2Provider.entries.find { it.name == value }
//                ?: throw IllegalArgumentException("매칭되는 플랫폼이 없습니다.")
//        }
//    }

}