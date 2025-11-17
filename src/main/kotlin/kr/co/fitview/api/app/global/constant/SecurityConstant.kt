package kr.co.fitview.api.app.global.constant

object SecurityConstant {


    const val API_BASE: String = "/api/*"

    val WS_STOMP_URI = "/ws-stomp"

    val ADMIN_URIS: List<String> = listOf(
        "$API_BASE/admin/**",
        "/webjars/**",
        "/docs/**"
    )


    val USER_URIS: List<String> = listOf(
        "$API_BASE/members/**",
        "$API_BASE/images/presign",
        "$API_BASE/oauth2/signup",
        "$API_BASE/addresses/**",
        "$API_BASE/workout-partners/**",
    )

    val PERMIT_ALL_URIS: List<String> = listOf(
        "$API_BASE/auth/**",
        "$API_BASE/docs/**",
        "$API_BASE/exception/**",
        "$API_BASE/oauth2/login",
        "$API_BASE/test/**",
        "$WS_STOMP_URI/**",

        "/css/**",
        "/js/**",
        "/images/**",
        "/fonts/**",
        "/static/**",
        "/public/**",
        "/resources/**",
        "/favicon.ico",
        "/error",
        "/h2-console/**",
        "/actuator/health",
        "/actuator/info",
        "/api/header",
    )


    val DOCS_URIS: List<String> = listOf(
        "/docs/**"
    )

    val CORS_PERMIT_URIS: List<String> = listOf(
        "$API_BASE/oauth2/**",
    )


}