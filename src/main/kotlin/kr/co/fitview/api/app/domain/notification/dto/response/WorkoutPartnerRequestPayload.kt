package kr.co.fitview.api.app.domain.notification.dto.response

data class WorkoutPartnerRequestContent(
    val payload: Payload
) {
    data class Payload(
        val memberId: Long,
        val workoutPartnerRequestId: Long
    )
}

data class WorkoutPartnerAcceptContent(
    val payload: Payload
) {
    data class Payload(
        val memberId: Long,
        val workoutPartnerRequestId: Long,
        val workoutPartnerId: Long
    )
}

data class WorkoutPartnerRejectContent(
    val payload: Payload
) {
    data class Payload(
        val memberId: Long,
        val workoutPartnerRequestId: Long
    )
}

data class WorkoutRequestContent(
    val payload: Payload
) {
    data class Payload(
        val workoutRequestId: Long,
        val chatRoomId: Long,
        val chatMessageId : Long
    )
}

data class WorkoutRequestAcceptContent(
    val payload: Payload
) {
    data class Payload(
        val workoutRequestId: Long,
        val chatRoomId: Long,
        val chatMessageId : Long
    )
}

data class WorkoutRequestRejectContent(
    val payload: Payload
) {
    data class Payload(
        val workoutRequestId: Long,
        val chatRoomId: Long,
        val chatMessageId : Long
    )
}

data class WorkoutCompleteContent(
    val payload: Payload
) {
    data class Payload(
        val workoutRequestId: Long,
        val workoutHistoryId: Long,
        val chatRoomId: Long,
        val chatMessageId : Long
    )
}

data class ReviewReceiveContent(
    val payload: Payload
) {
    data class Payload(
        val reviewId: Long,
        val workoutHistoryId: Long,
        val chatRoomId: Long
    )
}

data class ReviewRequestContent(
    val payload: Payload
) {
    data class Payload(
        val workoutHistoryId: Long,
        val chatMessageId : Long,
        val chatRoomId: Long
    )
}

