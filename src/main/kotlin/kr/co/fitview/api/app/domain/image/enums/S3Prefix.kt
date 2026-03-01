package kr.co.fitview.api.app.domain.image.enums

enum class S3Prefix(
    val value: String,
    val description : String
) {

    // 맨 왼쪽에 /를 빼야 S3 prefix에 맞게 들어감
    MEMBER_PROFILE("member/profile", "회원 프로필 사진"),
    MEMBER_WORKOUT("member/workout", "회원 운동 사진"),
    APP_FEEDBACK_BANNER("app-feedback/banner", "앱 피드백 배너 사진"),
    APP_FEEDBACK_CARD("app-feedback/card", "앱 피드백 카드 사진")

    ;

    override fun toString(): String {
        return "$name: $description"
    }

    companion object {
        fun allDescriptions(): String {
            return entries.joinToString(", ") { it.toString() }
        }
    }

}
