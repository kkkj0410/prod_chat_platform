package kr.co.fitview.api.app.domain.member.entity.enums

enum class MemberWorkoutExperience(val description : String) {

    JUST_STARTED("운동을 시작한 지 얼마 안 된 상태"),
    UNDER_ONE_YEAR("운동 경험 1년 미만"),
    ONE_TO_THREE_YEARS("운동 경험 1~3년"),
    FOUR_TO_SIX_YEARS("운동 경험 4~6년"),
    OVER_SEVEN_YEARS("운동 경험 7년 이상");

    override fun toString(): String {
        return "$name: $description"
    }

    companion object {
        fun allDescription(): List<String> {
            return entries.map { it.toString() }
        }
    }

}