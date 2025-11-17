package kr.co.fitview.api.app.global.exception.error.global

import kr.co.fitview.api.app.global.exception.error.ErrorCode

enum class GlobalErrorCode(
    override val rawCode: String,
    override val message: String,
    override val description: String
) : ErrorCode {

    ENTITY_NOT_FOUND("001", "Entity not found", "요청한 엔티티를 찾을 수 없음")
    ;

    override val prefix: String
        get() = "GLOBAL"

}