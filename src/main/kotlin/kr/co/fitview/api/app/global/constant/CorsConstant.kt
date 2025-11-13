package kr.co.fitview.api.app.global.constant

class CorsConstant {
    companion object {
        val ALLOW_ORIGIN_URIS = listOf(
            "http://localhost:*",
            "http://127.0.0.1:*",
            "https://fitview-8757f.web.app",
            "https://fitview.kr"
        )
    }
}