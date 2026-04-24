package kr.co.fitview.api.app.domain.banner.entity.enums

enum class BannerType(val description : String) {


    APP_FEEDBACK("앱 피드백"),
    WORKOUT_REWARD("운동 리워드"),
    ETC("테스트 용도. 실제 사용되는 값이 아님")

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