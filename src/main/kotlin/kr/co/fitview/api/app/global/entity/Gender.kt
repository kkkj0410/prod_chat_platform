package kr.co.fitview.api.app.global.entity

import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience

enum class Gender(val description : String) {

    MALE("여자"),
    FEMALE("남자");

    override fun toString(): String {
        return "$name: $description"
    }

    companion object {
        fun allDescription(): List<String> {
            return entries.map { it.toString() }
        }
    }

}