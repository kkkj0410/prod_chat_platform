package kr.co.fitview.api.app.global.enums

import kr.co.fitview.api.app.domain.member.dto.request.Age

enum class Direction(
    val description: String
) {
    ASC("오름차순 (작은 값 → 큰 값)"),
    DESC("내림차순 (큰 값 → 작은 값)");

    override fun toString(): String {
        return "$name: $description"
    }

    companion object {
        fun allDescription(): List<String> {
            return entries.map { it.toString() }
        }
    }
}