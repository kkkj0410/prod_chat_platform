package kr.co.fitview.api.app.domain.banner.entity.enums

enum class BannerType(val description : String) {


    APP_FEEDBACK("앱 피드백"),


    ;

    override fun toString(): String {
        return "$name: $description"
    }

    companion object {
        fun allDescription(): List<String> {
            return entries.map { it.toString() }
        }
    }
}