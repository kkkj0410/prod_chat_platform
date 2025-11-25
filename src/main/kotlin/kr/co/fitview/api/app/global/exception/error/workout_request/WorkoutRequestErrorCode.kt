package kr.co.fitview.api.app.global.exception.error.workout_request

import kr.co.fitview.api.app.global.exception.error.ErrorCode

enum class WorkoutRequestErrorCode(
    override val rawCode: String,
    override val message: String,
    override val description: String
) : ErrorCode {


    TERMINATED_WORKOUT_REQUEST_STATUS_CHANGE(
        "001",
        "Terminated workout request status change",
        "이미 상태가 종료된 운동 요청에 대한 상태 변화를 시도함"
    ),

    FROM_MEMBER_CANNOT_REJECT(
        "002",
        "Workout request cannot be rejected by the sender",
        "운동 요청을 한 사람이 거절을 시도함"
    ),

    TO_MEMBER_CANNOT_CANCEL(
        "003",
        "Workout request cannot be cancelled by the receiver",
        "운동 요청을 받은 사람이 취소를 시도함"
    ),


    CANNOT_COMPLETE_UNLESS_ACCEPTED(
        "004",
        "Cannot change to completed status unless workout request is accepted",
        "운동 요청이 수락 상태가 아니면 완료 상태로 변경할 수 없음"
    ),

    ALREADY_SAME_STATUS(
        "005",
        "Cannot change to the same status as current",
        "요청 상태가 이미 현재 상태와 동일하여 변경할 수 없음"
    )

    ;

    override val prefix: String
        get() = "WORKOUT_REQUEST"

}