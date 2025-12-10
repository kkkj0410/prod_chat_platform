package kr.co.fitview.api.app.domain.notification.dto.response

import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType

enum class LinkType(val description : String) {
    MEMBER_PROFILE("상대 회원 프로필 이동"),
    MEMBER_PROFILE_ME("본인 프로필 이동"),
    CHAT_ROOM("채팅방 이동"),
    CHAT_START("채팅 시작 가능. (채팅방이 있는지는 모르는 상태)"),
    REVIEW_WRITE("리뷰 작성 페이지 이동")


    ;
    override fun toString(): String {
        return "$name: $description"
    }

    companion object {
        fun allDescription(): List<String> {
            return entries.map { it.toString() }
        }
    }
}