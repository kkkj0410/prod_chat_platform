package kr.co.fitview.api.app.domain.fcm.enums

enum class FcmMessage(val title : String, val body : String, val deepLinkPath : String){

    WORKOUT_PARTNER_REQUEST(
        "새로운 핏버디 요청이 도착했어요.",
        "%s님이 함께 운동하고 싶어해요. 프로필을 확인해볼까요?",
        "member/%s"
    ),

    WORKOUT_PARTNER_ACCEPT(
        "👍🏻 핏버디 매칭 완료",
        "핏버디가 연결됐어요. 지금 바로 대화를 시작해보세요.",
        "member/%s"
    ),

    CHAT_MESSAGE(
        "%s",
        "새 메시지가 도착했어요.",
        "chat/%s"
    ),

    WORKOUT_REQUEST(
        "%s",
        "%s 운동 약속을 제안했어요.",
        "chat/%s"
    ),

    WORKOUT_REQUEST_ACCEPT(
        "📌 운동 약속 픽스 완료",
        "운동 약속이 확정됐어요. 약속 시간을 지켜주세요!",
        "chat/%s"
    ),

    WORKOUT_REQUEST_REJECT(
        "운동 약속 취소",
        "이번 약속은 어려워요. 다른 시간으로 다시 잡아볼까요?",
        "chat/%s"
    ),

    WORKOUT_COMPLETE(
        "💪 오운완",
        "오늘의 운동 완료! 이번 운동 어떠셨나요? 간단한 후기를 남겨주세요.",
        "chat/%s"
    ),

    REVIEW_RECEIVE(
        "💌 리뷰가 도착했어요.",
        "함께한 운동에 대해 후기가 도착했어요. 지금 확인해볼까요?",
        "my"
    ),

    REVIEW_REQUEST(
        "📝 후기를 남겨주세요.",
        "이번 운동은 어떠셨나요? 간단한 후기를 남겨주세요.",
        "chat/%s"
    )


    ;

    fun formatTitle(vararg args: Any) = title.format(*args)
    fun formatBody(vararg args: Any) = body.format(*args)
    fun formatDeepLinkPath(vararg args: Any) = deepLinkPath.format(*args)

}