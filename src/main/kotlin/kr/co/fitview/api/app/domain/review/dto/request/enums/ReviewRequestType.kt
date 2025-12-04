package kr.co.fitview.api.app.domain.review.dto.request.enums

import kr.co.fitview.api.app.domain.member.entity.enums.WorkoutTimeName

enum class ReviewRequestType(val description : String) {

    GOOD("좋아요"),
    NORMAL("보통이예요"),
    BAD("안좋아요")

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