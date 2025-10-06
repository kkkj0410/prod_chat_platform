package kr.co.fitview.api.app.global.constant

object SecurityConstant {


    const val API_BASE: String = "/api/*"

    val ADMIN_URIS: List<String> = listOf(
        "$API_BASE/admin/**",
        "/webjars/**",
        "/docs/**"
    )


    val USER_URIS: List<String> = listOf(

    )

    val PERMIT_ALL_URIS: List<String> = listOf(
        "$API_BASE/auth/**",
        "$API_BASE/docs/**",

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


}