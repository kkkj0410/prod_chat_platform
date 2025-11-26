package kr.co.fitview.api.app.domain.workout_partner.entity.enums

import com.fasterxml.jackson.annotation.JsonCreator

enum class WorkoutPartnerRequestContent(val index: Int, val description: String) {

    BURN(0, "불태워요"),
    ONE_MORE_SET(1, "한세트더"),
    SWEAT(2, "땀터진다"),
    TOGETHER(3, "함께해요"),
    CHALLENGE(4, "도전해요"),
    LIGHT(5, "가볍게요");

    override fun toString(): String {
        return "$index: $name($description)"
    }

    companion object {
        fun allDescription(): List<String> {
            return entries.map { it.toString() }
        }

        @JvmStatic
        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        fun fromIndex(index: Int): WorkoutPartnerRequestContent =
            entries.firstOrNull { it.index == index }
                ?: throw IllegalArgumentException("Invalid index: $index")
    }
}