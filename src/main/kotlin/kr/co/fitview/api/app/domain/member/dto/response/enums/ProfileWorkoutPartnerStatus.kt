package kr.co.fitview.api.app.domain.member.dto.response.enums

enum class ProfileWorkoutPartnerStatus(val description : String) {
    NONE("아무 관계 아님. 요청하지도 않고, 요청 받지도 않음."),
    SEND("본인 -> 상대에게 요청을 보낸 상태"),
    RECEIVE("상대 -> 본인에게 요청을 보낸 상태"),
    PARTNER("본인과 상대는 운동 파트너 관계")

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