package kr.co.fitview.api.app.domain.member.entity.enums

enum class MemberWithdrawReasonReasonType(
    val description : String
) {

    FOUND_MATE("이미 운동 메이트를 찾았고, 만족스러운 관계를 유지하고 있어요."),
    NO_DESIRED_MATCH("원하는 지역/시간대에 맞는 운동 메이트를 찾기 어려웠어요."),
    UNRELIABLE_MATE("메이트와의 소통/약속 관리가 불편했고, 신뢰하기 어려웠어요."),
    USE_OTHER_SERVICE("다른 운동 앱/커뮤니티를 사용하게 되었어요."),
    APP_INCONVENIENCE("앱 사용에 전반적인 불편함(버그, 속도, UX 등)이 많았어요."),
    OTHER("기타");

    override fun toString(): String {
        return "$name: $description"
    }

    companion object {
        fun allDescription(): List<String> {
            return entries.map { it.toString() }
        }
    }
}