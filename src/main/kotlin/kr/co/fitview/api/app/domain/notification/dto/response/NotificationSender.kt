package kr.co.fitview.api.app.domain.notification.dto.response

data class NotificationSender(
    val memberId: Long,
    val nickname: String,
    val profileImageUrl: String
)