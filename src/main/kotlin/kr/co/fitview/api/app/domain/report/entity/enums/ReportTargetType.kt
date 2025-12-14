package kr.co.fitview.api.app.domain.report.entity.enums

import kr.co.fitview.api.app.domain.notification.entity.enums.NotificationType

enum class ReportTargetType(val description : String) {

    MEMBER("회원"),
    CHAT_ROOM("채팅방")

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