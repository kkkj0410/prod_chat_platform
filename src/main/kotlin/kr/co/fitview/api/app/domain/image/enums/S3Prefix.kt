package kr.co.fitview.api.app.domain.image.enums

enum class S3Prefix(
    val value: String,
    val description : String
) {

    // 맨 왼쪽에 /를 빼야 S3 prefix에 맞게 들어감
    MEMBER_PROFILE("member/profile", "MEMBER_PROFILE = 회원 프로필 사진"),
    MEMBER_WORKOUT("member/workout", "MEMBER_WORKOUT = 회원 운동 사진")

    ;

    companion object {
        fun allDescriptions(): String {
            return entries.joinToString(", ") { it.description }
        }
    }


//    companion object {
//        fun from(value: String): Gender {
//            return entries.find { it.name == value }
//                ?: throw IllegalArgumentException("매칭되는 역할이 없습니다.")
//        }
//    }

}
