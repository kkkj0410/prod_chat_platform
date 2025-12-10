package kr.co.fitview.api.app.domain.notification.dto.response

data class WorkoutPartnerRequestContent(
    val sender: NotificationSender,
    val payload: Payload
) {
    data class Payload(
        val memberId: Long,
        val workoutPartnerRequestId: Long
    )
}

data class WorkoutPartnerAcceptContent(
    val sender: NotificationSender,
    val payload: Payload
) {
    data class Payload(
        val memberId: Long,
        val workoutPartnerRequestId: Long,
        val workoutPartnerId: Long
    )
}

data class WorkoutPartnerRejectContent(
    val sender: NotificationSender,
    val payload: Payload
) {
    data class Payload(
        val memberId: Long,
        val workoutPartnerRequestId: Long
    )
}

data class WorkoutRequestContent(
    val sender: NotificationSender,
    val payload: Payload
) {
    data class Payload(
        val workoutRequestId: Long,
        val chatRoomId: Long
    )
}

data class WorkoutRequestAcceptContent(
    val sender: NotificationSender,
    val payload: Payload
) {
    data class Payload(
        val workoutRequestId: Long,
        val chatRoomId: Long
    )
}

data class WorkoutRequestRejectContent(
    val sender: NotificationSender,
    val payload: Payload
) {
    data class Payload(
        val workoutRequestId: Long,
        val chatRoomId: Long
    )
}

data class WorkoutCompleteContent(
    val sender: NotificationSender,
    val payload: Payload
) {
    data class Payload(
        val workoutRequestId: Long,
        val workoutHistoryId: Long,
        val chatRoomId: Long
    )
}

data class ReviewReceiveContent(
    val sender: NotificationSender,
    val payload: Payload
) {
    data class Payload(
        val reviewId: Long,
        val workoutHistoryId: Long,
        val chatRoomId: Long
    )
}

data class ReviewRequestContent(
    val sender: NotificationSender,
    val payload: Payload
) {
    data class Payload(
        val workoutHistoryId: Long,
        val chatRoomId: Long
    )
}

