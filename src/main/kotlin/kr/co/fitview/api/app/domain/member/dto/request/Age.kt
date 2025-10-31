package kr.co.fitview.api.app.domain.member.dto.request

import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle

enum class Age(val min: Int, val max: Int, val label: String) {

    TEEN_LATE(1, 19, "10대 후반"),
    TWENTIES_EARLY(20, 23, "20대 초반"),
    TWENTIES_MID(24, 26, "20대 중반"),
    TWENTIES_LATE(27, 29, "20대 후반"),
    THIRTIES_EARLY(30, 33, "30대 초반"),
    THIRTIES_MID(34, 36, "30대 중반"),
    THIRTIES_LATE(37, 39, "30대 후반"),
    FORTIES_EARLY(40, 43, "40대 초반"),
    FORTIES_MID(44, 46, "40대 중반"),
    FORTIES_LATE(47, 49, "40대 후반"),
    FIFTIES_AND_ABOVE(50, Int.MAX_VALUE, "50대 이상");

    override fun toString(): String {
        return "$name: $label"
    }

    companion object {
        fun allDescription(): List<String> {
            return entries.map { it.toString() }
        }
    }
}