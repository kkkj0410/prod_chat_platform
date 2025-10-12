package kr.co.fitview.api.app.global.config

import kr.co.fitview.api.app.global.constant.SecurityConstant
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.filter.JwtAuthenticationFilter
import kr.co.fitview.api.app.global.filter.SecurityExceptionFilter
import kr.co.fitview.api.app.global.jwt.JwtAuthentication
import kr.co.fitview.api.app.global.security.JwtAuthenticationEntryPoint
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.filter.ForwardedHeaderFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    val corsConfig : CorsConfig,
    val jwtAuthenticationFilter: JwtAuthenticationFilter,
    val jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint,
    val securityExceptionFilter: SecurityExceptionFilter
) {

    @Bean
    fun forwardedHeaderFilter(): ForwardedHeaderFilter {
        return ForwardedHeaderFilter()
    }

    @Bean
    fun authenticationManager(authenticationConfiguration: AuthenticationConfiguration): AuthenticationManager {
        return authenticationConfiguration.authenticationManager
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { csrf -> csrf.disable() }
            .formLogin { form -> form.disable() }
            .cors { cors ->
                cors.configurationSource(corsConfig.corsConfigurationSource())
//                cors.configurationSource { request ->
//                    if (SecurityConstant.CORS_PERMIT_URIS.any { request.requestURI.startsWith(it) }) {
//                        return@configurationSource null
//                    }
//                    corsConfig.corsConfigurationSource().getCorsConfiguration(request)
//                }
            }

            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests{auth ->
                auth
                    .requestMatchers(*SecurityConstant.ADMIN_URIS.toTypedArray())
                        .hasRole(Role.ADMIN.toString())
                    .requestMatchers(*SecurityConstant.USER_URIS.toTypedArray())
                        .hasAnyRole(Role.USER.toString())
                    .requestMatchers(*SecurityConstant.PERMIT_ALL_URIS.toTypedArray())
                        .permitAll()
                    .anyRequest().denyAll()
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(securityExceptionFilter, JwtAuthenticationFilter::class.java)
            .exceptionHandling { exception: ExceptionHandlingConfigurer<HttpSecurity?> ->
                exception
                    .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            }



//            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
//            .addFilterBefore(securityExceptionFilter, JwtAuthenticationFilter::class.java)
//            .exceptionHandling { exception: ExceptionHandlingConfigurer<HttpSecurity?> ->
//                exception
//                    .authenticationEntryPoint(authenticationEntryPoint)
//            }

        return http.build()
    }
}