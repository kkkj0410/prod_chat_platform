package kr.co.fitview.api.app.domain.report.entity.enums

enum class ReportReasonType(val type : ReportTargetType?, val description : String) {

    NO_SHOW(
        ReportTargetType.MEMBER,
        "약속시간 미준수 / 노쇼"
    ),
    EXCESSIVE_PERSONAL_INFO_REQUEST(
        ReportTargetType.MEMBER,
        "과도한 개인 정보를 요구"
    ),
    RUDE_OR_SEXUAL_HARASSMENT(
        ReportTargetType.MEMBER,
        "무례한 발언, 성희롱 등 부적절한 언행"
    ),
    FALSE_INFORMATION(
        ReportTargetType.MEMBER,
        "허위 정보를 기재"
    ),
    UNWANTED_BEHAVIOR(
        ReportTargetType.MEMBER,
        "원치 않는 불쾌한 행동"
    ),
    COMMERCIAL_APPROACH(
        ReportTargetType.MEMBER,
        "영업을 목적으로 접근"
    ),


    RUDE_LANGUAGE(
        ReportTargetType.CHAT_ROOM,
        "불쾌한 언행"
    ),
    THREAT_OR_INTIMIDATION(
        ReportTargetType.CHAT_ROOM,
        "협박 또는 위협적인 내용"
    ),
    PRESSURING_APPOINTMENT(
        ReportTargetType.CHAT_ROOM,
        "약속을 강요하거나 압박"
    ),
    PERSONAL_INFO_REQUEST(
        ReportTargetType.CHAT_ROOM,
        "개인정보를 요구"
    ),
    INAPPROPRIATE_REQUEST(
        ReportTargetType.CHAT_ROOM,
        "부적절한 요청"
    ),
    FRAUD_OR_MONEY_REQUEST(
        ReportTargetType.CHAT_ROOM,
        "사기 또는 금전 요구 의심"
    ),


    OTHER(
        null,
        "기타"
    );

    override fun toString(): String {
        val targetCategory = type?.name ?: "공통"

        return "[$targetCategory] $name : $description"
    }

    companion object {
        fun allDescription(): List<String> {
            return entries.map { it.toString() }
        }
    }

}