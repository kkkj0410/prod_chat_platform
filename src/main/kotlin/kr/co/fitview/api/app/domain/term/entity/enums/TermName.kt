package kr.co.fitview.api.app.domain.term.entity.enums

enum class TermName(val isRequired : Boolean) {

    AGE_OVER_14(true), PRIVACY_POLICY(true), TERMS_OF_SERVICE(true), LOCATION_SERVICE(true)

//    companion object {
//        fun from(value: String): Gender {
//            return entries.find { it.name == value }
//                ?: throw IllegalArgumentException("매칭되는 역할이 없습니다.")
//        }
//    }

}