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
    APPLE_JWT_SIGNED_KEY_NOT_FOUND("004", "apple jwt signing key not found", "애플에 요청해서 서명키 목록을 받았는데, 해당 apple jwt 토큰에 서명으로 사용된 서명키가 없음. apple에서 발급된 jwt 토큰이 아닌 것으로 간주하고 프로필 조회 안함"),
    APPLE_SIGNED_INVALID("005", "apple jwt signed invalid", "애플 응답받은 jwt 토큰의 서명란이 유효하지 않음"),
    APPLE_ISSUER_INVALID("006", "apple jwt issuer invalid", "애플 응답받은 jwt 토큰의 issuer가 유효하지 않음"),
    APPLE_AUDIENCE_INVALID("006", "apple jwt audience invalid", "애플 응답받은 jwt 토큰의 audience가 유효하지 않음"),
    APPLE_JWT_EXPIRED("006", "apple jwt expired", "애플 응답받은 jwt 토큰의 유효기간 지났음"),
    APPLE_SUBJECT_INVALID("007", "apple not found subject", "애플 jwt 토큰에서 고유 id를 찾을 수 없음"),
    APPLE_KEY_INVALID_FORMAT("008", "apple key invalid format", "apple key는 BEGIN PRIVATE KEY ~ END PRIVATE KEY 형식인데, 해당 형식이 아님. 따라서, 애플 비밀키가 아닌 것으로 간주하고 에러"),
    APPLE_SIGN_KEY_GENERATION_FAILED("009", "apple sign key generation failed", "apple key로 애플 프로필 요청 jwt 토큰에 쓰이는 서명키 제작을 시도했음. 하지만 애플키가 서명키 제작 형식에 맞지 않아 에러. 즉, 애플키 값이 유효하지 않음"),

    KAKAO_POST_FAILED("010", "Failed to request Kakao profile with accessToken.", "카카오 accessToken으로 카카오 프로필 조회 요청. 하지만 프로필 조회에 실패. 사유 - 잘못된 accessToken 등"),

    DUPLICATE_SOCIAL_MEMBER("011", "Duplicate social member signup", "소셜 로그인 회원가입을 중복해서 하면 회원 정보 등록을 거부함"),

    GOOGLE_AUTH_CODE_EXCHANGE_FAILED("012", "Failed to exchange authCode for accessToken.", "구글 authCode를 이용해 accessToken 발급 요청을 시도했지만 실패함. 사유 - 잘못된 authCode 또는 클라이언트 설정 불일치 등"),
    GOOGLE_ACCESS_TOKEN_PARSE_FAILED("013", "Failed to parse accessToken from Google response.", "구글로부터 accessToken 발급 응답을 받았지만, 응답 내에서 accessToken 값을 파싱하지 못함. 사유 - 응답 포맷 변경 또는 JSON 파싱 오류 등"),
    GOOGLE_PROFILE_REQUEST_FAILED("014", "Failed to request Google profile with accessToken.", "구글 accessToken으로 구글 프로필 조회 요청을 시도했지만 실패함. 사유 - 만료되었거나 잘못된 accessToken 등"),
    GOOGLE_PROFILE_ID_MISSING("015", "Google profile does not contain a unique ID.", "구글 프로필 응답을 받았지만 고유 식별자(id) 필드가 존재하지 않음. 사유 - 잘못된 scope 설정 또는 응답 포맷 오류 등"),
    GOOGLE_PROFILE_EMAIL_MISSING("016", "Google profile does not contain an email.", "구글 프로필 응답을 받았지만 이메일(email) 필드가 존재하지 않음. 사유 - 사용자 이메일 비공개 설정 또는 scope 누락 등"),






















    ;

    override val prefix: String
        get() = "OAUTH2"

}