package kr.co.fitview.api.app.global.exception.error.oauth2

import kr.co.fitview.api.app.global.exception.error.ErrorCode

enum class OAuth2ErrorCode(
    override val rawCode: String,
    override val message: String,
    override val description: String
) : ErrorCode {

    APPLE_POST_FAILED("001", "Invalid apple authorization code", "인증 code로 요청을 보냈으나, apple에게 프로필 조회 받는 것을 거부 당함(이미 사용한 code를 재사용할 경우, apple에서 응답을 거부할 수도 있음) - 에러가 발생한다면 redirect uri, auth code가 유효한지 확인 필요"),
    APPLE_JWT_NOT_FOUND("002", "apple response not found jwt(id token)","auth code로 애플에게 응답을 받았는데, 응답값에 id_token(프로필 있는 곳)필드가 없음"),
    APPLE_EMAIL_NOT_FOUND("003", "apple response not found email", "애플에게 응답을 받았는데, 없는 회원이라서 회원가입시키려고 했음. 근데, email 저장하려고 봤더니 email 값이 없음"),
    APPLE_JWT_CONVERTER_FAIL("004", "apple jwt converter fail", "애플 응답 jwt 토큰을 RSA로 변환하는 과정에서 실패"),
    APPLE_SIGNED_INVALID("005", "apple jwt signed invalid", "애플 응답받은 jwt 토큰의 서명란이 유효하지 않음"),
    APPLE_ISSUER_INVALID("006", "apple jwt issuer invalid", "애플 응답받은 jwt 토큰의 issuer가 유효하지 않음"),
    APPLE_AUDIENCE_INVALID("006", "apple jwt audience invalid", "애플 응답받은 jwt 토큰의 audience가 유효하지 않음"),
    APPLE_JWT_EXPIRED("006", "apple jwt expired", "애플 응답받은 jwt 토큰의 유효기간 지났음")



    ;

    override val prefix: String
        get() = "OAUTH2"

}