package kr.co.fitview.api.app.global.config

import kr.co.fitview.api.app.global.constant.CorsConstant
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource


@Configuration
class CorsConfig {

    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()

        //리소스를 허용할 URL 지정
        configuration.setAllowedOriginPatterns(
            CorsConstant.ALLOW_ORIGIN_URIS
        )

        //허용하는 HTTP METHOD 지정
        val allowedHttpMethods = ArrayList<String>()
        allowedHttpMethods.add("GET")
        allowedHttpMethods.add("POST")
        allowedHttpMethods.add("PUT")
        allowedHttpMethods.add("DELETE")
        configuration.allowedMethods = allowedHttpMethods

        configuration.allowedHeaders = listOf("*")

        //configuration.setAllowedHeaders(List.of(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE));

        //인증, 인가를 위한 credentials 를 TRUE로 설정
        configuration.allowCredentials = true

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)

        return source
    }

}