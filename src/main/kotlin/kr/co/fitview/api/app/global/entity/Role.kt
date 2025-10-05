package kr.co.fitview.api.app.global.entity

enum class Role {

    USER, ADMIN;

    companion object {
        fun from(value: String): Role {
            return entries.find { it.name == value }
                ?: throw IllegalArgumentException("매칭되는 역할이 없습니다.")
        }
    }

    fun toRoleName() : String{
        return "ROLE_$name"
    }
}