package kr.co.fitview.api.app.global.exception.error.workout_partner

import kr.co.fitview.api.app.global.exception.error.ErrorCode

enum class WorkoutPartnerErrorCode(
    override val rawCode: String,
    override val message: String,
    override val description: String
) : ErrorCode {

    PARTNER_REQUEST_COOLDOWN("001", "Workout partner request blocked", "24시간 내에 이미 파트너 요청을 보냈기 때문에 추가 요청이 불가능함(or 24시간 내에 상대방에게 취소를 당하지 않았기 때문에 핏버디 요청 불가)"),
    ALREADY_PARTNER_ACCEPTED("002", "Workout partner already accepted", "이미 해당 회원과 파트너 관계가 성립되어 있어 새로운 요청을 보낼 수 없음");

    ;

    override val prefix: String
        get() = "WORKOUT_PARTNER"

}