package kr.co.fitview.api.app.global.dto

data class WsResponse<T>(
    val type: String,
    val payload: T
)

interface WsTypeIdentifier {
    val code: String
}


enum class WsMessageType(
    override val code: String
) : WsTypeIdentifier {
    TEXT("TEXT"),
    WORKOUT_REQUEST("WORKOUT_REQUEST"),

    WORKOUT_REQUEST_UPDATE("WORKOUT_REQUEST_UPDATE"),

    WORKOUT_PARTNER_REQUEST("WORKOUT_PARTNER_REQUEST"),
    WORKOUT_PARTNER_ACCEPT("WORKOUT_PARTNER_ACCEPT"),
}