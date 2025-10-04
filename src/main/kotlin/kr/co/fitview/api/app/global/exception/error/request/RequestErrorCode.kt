package kr.co.fitview.api.app.global.exception.error.request

import kr.co.fitview.api.app.global.dto.ApiResponse
import kr.co.fitview.api.app.global.exception.error.ErrorCode
import org.springframework.http.HttpStatus

enum class RequestErrorCode (
    override val rawCode: String,
    override val message: String
) : ErrorCode {

    REQ_FIELD_NOT_VALID("001", ""),
    REQ_HEADER_NOT_VALID("002", "Required request header is not valid."),
    REQ_ENUM_MISMATCH("003", "The provided value does not match any valid enum.")

    ;

    override val prefix: String
        get() = "REQ"

    override fun toApiResponse(): ApiResponse<Any?> =
        ApiResponse.error(HttpStatus.BAD_REQUEST, code, message)
}