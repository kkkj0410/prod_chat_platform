package kr.co.fitview.api.app.domain.chat.entity.enums

import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal

enum class ChatMessageType(val description : String) {

    TEXT("string 문자열 문자 메시지"),
    WORKOUT_REQUEST("운동 요청")

    ;

    override fun toString(): String {
        return "$name: $description"
    }

    companion object {
        fun allDescription(): List<String> {
            return MemberWorkoutGoal.entries.map { it.toString() }
        }
    }
}