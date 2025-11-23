package kr.co.fitview.api.app.global.dto

data class WsResponse<T>(
    val type: WsMessageType,
    val payload: T
)

enum class WsMessageType {
    TEXT,
    WORKOUT_REQUEST,
}