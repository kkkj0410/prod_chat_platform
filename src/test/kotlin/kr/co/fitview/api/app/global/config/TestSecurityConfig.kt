package kr.co.fitview.api.app.global.config


import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.*
import org.springframework.security.web.SecurityFilterChain


@TestConfiguration
class TestSecurityConfig {

//    @MockitoBean
//    private lateinit var jwtAuthentication: JwtAuthentication
//
//    @MockitoBean
//    private lateinit var jwtAuthenticationFilter: JwtAuthenticationFilter
//
//    @MockitoBean
//    private lateinit var securityExceptionFilter: SecurityExceptionFilter
//
//    @MockitoBean
//    private lateinit var jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint


    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf{obj: CsrfConfigurer<HttpSecurity> -> obj.disable()}
            .authorizeHttpRequests{it.anyRequest().permitAll()}
        return http.build()
    }

}