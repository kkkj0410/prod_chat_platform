package kr.co.fitview.api.app.global.exception.workout_reward

import kr.co.fitview.api.app.global.exception.error.ErrorCode

enum class WorkoutRewardErrorCode(
    override val rawCode: String,
    override val message: String,
    override val description: String
) : ErrorCode {

    ALREADY_COUPON_REQUESTED("001", "Coupon already requested", "이미 해당 쿠폰을 발급받았거나 요청한 상태이므로 다시 요청할 수 없음"),
    COUPON_CONDITION_NOT_MET("002", "Coupon condition not met", "쿠폰을 발급받기 위한 조건(리워드 스탬프 개수 등)을 충족하지 못해 요청할 수 없음")
    ;

    override val prefix: String
        get() = "WORKOUT_REWARD"

}