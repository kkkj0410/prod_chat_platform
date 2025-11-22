package kr.co.fitview.api.app.global.constant

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component


@Component
class CorsConstant(
    @Value("\${cors.allow-origin-localhost}")
    private val localhost: String,

    @Value("\${cors.allow-origin-127}")
    private val local127: String,

    @Value("\${cors.allow-origin-webapp}")
    private val webapp: String,

    @Value("\${cors.allow-origin-domain}")
    private val domain: String,
) {
    val ALLOW_ORIGIN_URIS : List<String> = listOf(
        localhost,
        local127,
        webapp,
        domain
    )
}
