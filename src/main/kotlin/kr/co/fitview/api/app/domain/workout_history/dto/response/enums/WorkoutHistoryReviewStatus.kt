package kr.co.fitview.api.app.domain.workout_history.dto.response.enums

import kr.co.fitview.api.app.domain.term.entity.enums.TermName

enum class WorkoutHistoryReviewStatus(
    val description : String
){
    WRITABLE("후기 작성 가능"),
    WRITTEN("후기 작성이 이미 끝남"),
    EXPIRED("만료되어 후기 작성 불가"),


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
