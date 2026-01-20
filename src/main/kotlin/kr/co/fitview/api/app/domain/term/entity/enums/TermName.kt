package kr.co.fitview.api.app.domain.term.entity.enums

enum class TermName(val isRequired: Boolean, val description: String) {

    PRIVACY_POLICY(true, "개인정보 처리 방침 동의"),
    TERMS_OF_SERVICE(true, "서비스 이용 약관 동의"),
    LOCATION_SERVICE(true, "위치 기반 서비스 이용 동의");

    override fun toString(): String {
        return "$name: $description (필수: $isRequired)"
    }

    companion object {
        fun allDescription(): List<String> {
            return entries.map { it.toString() }
        }
    }
}