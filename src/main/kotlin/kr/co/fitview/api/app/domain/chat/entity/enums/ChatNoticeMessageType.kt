package kr.co.fitview.api.app.domain.chat.entity.enums

enum class ChatNoticeMessageType(val description : String) {

    WORKOUT_REQUEST_ACCEPT("운동 예약 확정"),
    WORKOUT_REQUEST_CANCEL("운동 취소"),
    WORKOUT_REQUEST_REJECT("운동 취소"),
    WORKOUT_REQUEST_EXPIRE("운동 만료"),
    WORKOUT_REQUEST_COMPLETE("운동 완료")

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