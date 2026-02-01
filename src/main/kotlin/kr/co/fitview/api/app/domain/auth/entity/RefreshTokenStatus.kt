package kr.co.fitview.api.app.domain.auth.entity

enum class RefreshTokenStatus(val description : String) {


    ACTIVE("활성화"),
    EXPIRED("만료된"),
    REVOKED("취소된")


    ;
}