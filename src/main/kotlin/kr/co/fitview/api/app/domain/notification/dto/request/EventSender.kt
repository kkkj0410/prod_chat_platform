package kr.co.fitview.api.app.domain.notification.dto.request

data class EventSender(
    val memberId : Long,
    val nickname : String,
    val profileImageUrl : String
)
