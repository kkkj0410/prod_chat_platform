package kr.co.fitview.api.app.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.util.AntPathMatcher


@Configuration
class PathConfig {

    @Bean
    fun antPathMatcher(): AntPathMatcher {
        return AntPathMatcher()
    }
}