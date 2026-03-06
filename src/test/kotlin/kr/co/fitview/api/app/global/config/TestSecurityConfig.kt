package kr.co.fitview.api.app.global.config


import jakarta.annotation.PostConstruct
import kr.co.fitview.api.app.global.util.SecurityHolder
import kr.co.fitview.api.app.global.util.SecurityProvider
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.*
import org.springframework.security.web.SecurityFilterChain


@TestConfiguration
class TestSecurityConfig {

    @PostConstruct
    fun initTestSecurityHolder() {
        SecurityHolder.provider = object : SecurityProvider {
            override fun getMemberId(): Long = 1L
            override fun getMemberIdOrNull(): Long = 1L
        }
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf{obj: CsrfConfigurer<HttpSecurity> -> obj.disable()}
            .authorizeHttpRequests{it.anyRequest().permitAll()}
        return http.build()
    }

}