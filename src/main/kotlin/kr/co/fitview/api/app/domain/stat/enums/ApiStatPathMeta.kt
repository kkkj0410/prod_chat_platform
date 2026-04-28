package kr.co.fitview.api.app.domain.stat.enums

import kr.co.fitview.api.app.domain.stat.entity.enums.ApiStatMethod

enum class ApiStatPathMeta(
    val method: ApiStatMethod,
    val path: String,
    val description: String,
    val isSignificantStat: Boolean,
) {
    MEMBERS_LOCAL(
        method = ApiStatMethod.GET,
        path = "/api/v1/members/local/{id}",
        description = "우리 동네 핏버디 조회",
        isSignificantStat = true
    ),

    MEMBERS_REVIEWS(
        method = ApiStatMethod.GET,
        path = "/api/v1/members/{id}/reviews",
        description = "회원 프로필 후기 조회",
        isSignificantStat = true
    ),

    MEMBERS_REVIEWS_TAGS(
        method = ApiStatMethod.GET,
        path = "/api/v1/members/{id}/reviews/tags",
        description = "회원 프로필 서브 후기 조회",
        isSignificantStat = true
    ),

    MEMBERS_ADDRESSES(
        method = ApiStatMethod.GET,
        path = "/api/v1/members/addresses",
        description = "회원 주소 조회",
        isSignificantStat = false
    ),

    MEMBERS_RECOMMENDATIONS(
        method = ApiStatMethod.GET,
        path = "/api/v1/members/recommendations",
        description = "추천 핏버디 조회",
        isSignificantStat = true
    ),

    NOTIFICATIONS_READ(
        method = ApiStatMethod.GET,
        path = "/api/v1/notifications/read",
        description = "인앱 알람 읽음 여부 조회",
        isSignificantStat = false
    ),

    MEMBERS_PROFILE(
        method = ApiStatMethod.GET,
        path = "/api/v1/members/{id}",
        description = "상대 회원 프로필 조회",
        isSignificantStat = true
    ),

    CHATS_MESSAGES(
        method = ApiStatMethod.GET,
        path = "/api/v1/chats/{id}/messages",
        description = "채팅방 내 메시지 조회",
        isSignificantStat = true
    ),

    CHATS_WORKOUT_REQUEST_LAST(
        method = ApiStatMethod.GET,
        path = "/api/v1/chats/{id}/workout-requests/last",
        description = "채팅방 마지막 운동 요청 조회",
        isSignificantStat = false
    ),

    AUTH_REFRESH(
        method = ApiStatMethod.POST,
        path = "/api/v1/auth/refresh",
        description = "로그인 토큰 갱신",
        isSignificantStat = false
    ),

    MEMBERS_ME(
        method = ApiStatMethod.GET,
        path = "/api/v1/members/me",
        description = "본인 프로필 조회",
        isSignificantStat = true
    ),

    CHATS_MEMBERS(
        method = ApiStatMethod.GET,
        path = "/api/v1/chats/{id}/members",
        description = "채팅방 서로 회원 조회",
        isSignificantStat = false
    ),

    CHATS_READ(
        method = ApiStatMethod.PATCH,
        path = "/api/v1/chats/{id}/read",
        description = "채팅방 메시지 전체 읽음 처리",
        isSignificantStat = false
    ),

    CHATS(
        method = ApiStatMethod.GET,
        path = "/api/v1/chats",
        description = "채팅방 조회",
        isSignificantStat = true
    ),

    FCM_TOKENS(
        method = ApiStatMethod.POST,
        path = "/api/v1/fcm-tokens",
        description = "푸시 알람 토큰 갱신",
        isSignificantStat = false
    ),

    NOTIFICATIONS(
        method = ApiStatMethod.GET,
        path = "/api/v1/notifications",
        description = "인앱 알람 조회",
        isSignificantStat = true
    ),

    AUTH_LOGIN(
        method = ApiStatMethod.POST,
        path = "/api/v1/auth/login",
        description = "로컬 로그인",
        isSignificantStat = false
    ),

    WORKOUT_PARTNER_REQUESTS(
        method = ApiStatMethod.GET,
        path = "/api/v1/workout-partner-requests",
        description = "본인 계정 대상 운동 파트너 보낸/받은 요청 이력 조회",
        isSignificantStat = true
    ),

    OAUTH2_LOGIN(
        method = ApiStatMethod.POST,
        path = "/api/v1/oauth2/login",
        description = "소셜 로그인",
        isSignificantStat = true
    ),

    BANNER_ACTIVE(
        method = ApiStatMethod.GET,
        path = "/api/v1/banners/active",
        description = "배너 조회",
        isSignificantStat = true
    ),

    WORKOUT_REWARD_STAMP_ME(
        method = ApiStatMethod.GET,
        path = "/api/v1/workout-rewards/stamps/me",
        description = "운동 리워드 스탬프 개수 조회",
        isSignificantStat = true
    ),

    IMAGE_PRESIGN(
        method = ApiStatMethod.POST,
        path = "/api/v1/images/presign",
        description = "이미지 업로드 url 요청",
        isSignificantStat = false
    ),


    ALL_GET_ADMINS(
        method = ApiStatMethod.GET,
        path = "/api/v1/admins/*",
        description = "어드민 API",
        isSignificantStat = false
    ),

    ALL_POST_ADMINS(
        method = ApiStatMethod.POST,
        path = "/api/v1/admins/*",
        description = "어드민 API",
        isSignificantStat = false
    )




    ;

    companion object {
        fun find(method: ApiStatMethod, path: String): ApiStatPathMeta? {
            return entries.firstOrNull { it.method == method && it.path == path }
        }

        fun getDescription(method: ApiStatMethod, path: String): String {
            return find(method, path)?.description ?: "설명 없음"
        }
    }
}
