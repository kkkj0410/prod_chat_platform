package kr.co.fitview.api.app.domain.stat.enums

import kr.co.fitview.api.app.domain.stat.entity.enums.ApiStatMethod

enum class ApiStatPathMeta(
    val method: ApiStatMethod,
    val path: String,
    val description: String,
    val excludeFromTop: Boolean,
) {
    MEMBERS_LOCAL(
        method = ApiStatMethod.GET,
        path = "/api/v1/members/local/{id}",
        description = "우리 동네 핏버디 조회",
        excludeFromTop = false
    ),

    MEMBERS_REVIEWS(
        method = ApiStatMethod.GET,
        path = "/api/v1/members/{id}/reviews",
        description = "회원 프로필 후기 조회",
        excludeFromTop = false
    ),

    MEMBERS_ADDRESSES(
        method = ApiStatMethod.GET,
        path = "/api/v1/members/addresses",
        description = "회원 주소 조회",
        excludeFromTop = true
    ),

    MEMBERS_RECOMMENDATIONS(
        method = ApiStatMethod.GET,
        path = "/api/v1/members/recommendations",
        description = "추천 핏버디 조회",
        excludeFromTop = false
    ),

    NOTIFICATIONS_READ(
        method = ApiStatMethod.GET,
        path = "/api/v1/notifications/read",
        description = "인앱 알람 읽음 여부 조회",
        excludeFromTop = true
    ),

    MEMBERS_PROFILE(
        method = ApiStatMethod.GET,
        path = "/api/v1/members/{id}",
        description = "상대 회원 프로필 조회",
        excludeFromTop = false
    ),

    CHATS_MESSAGES(
        method = ApiStatMethod.GET,
        path = "/api/v1/chats/{id}/messages",
        description = "채팅방 내 메시지 조회",
        excludeFromTop = false
    ),

    CHATS_WORKOUT_REQUEST_LAST(
        method = ApiStatMethod.GET,
        path = "/api/v1/chats/{id}/workout-requests/last",
        description = "채팅방 마지막 운동 요청 조회",
        excludeFromTop = true
    ),

    AUTH_REFRESH(
        method = ApiStatMethod.POST,
        path = "/api/v1/auth/refresh",
        description = "로그인 토큰 갱신",
        excludeFromTop = true
    ),

    MEMBERS_ME(
        method = ApiStatMethod.GET,
        path = "/api/v1/members/me",
        description = "본인 프로필 조회",
        excludeFromTop = false
    ),

    CHATS_MEMBERS(
        method = ApiStatMethod.GET,
        path = "/api/v1/chats/{id}/members",
        description = "채팅방 서로 회원 조회",
        excludeFromTop = true
    ),

    CHATS_READ(
        method = ApiStatMethod.PATCH,
        path = "/api/v1/chats/{id}/read",
        description = "채팅방 메시지 전체 읽음 처리",
        excludeFromTop = true
    ),

    FCM_TOKENS(
        method = ApiStatMethod.POST,
        path = "/api/v1/fcm-tokens",
        description = "푸시 알람 토큰 갱신",
        excludeFromTop = true
    ),

    NOTIFICATIONS(
        method = ApiStatMethod.GET,
        path = "/api/v1/notifications",
        description = "인앱 알람 조회",
        excludeFromTop = false
    );

    companion object {
        fun find(method: ApiStatMethod, path: String): ApiStatPathMeta? {
            return entries.firstOrNull { it.method == method && it.path == path }
        }

        fun isExcluded(method: ApiStatMethod, path: String): Boolean {
            return find(method, path)?.excludeFromTop ?: false
        }

        fun getDescription(method: ApiStatMethod, path: String): String {
            return find(method, path)?.description ?: "설명 없음"
        }
    }
}
