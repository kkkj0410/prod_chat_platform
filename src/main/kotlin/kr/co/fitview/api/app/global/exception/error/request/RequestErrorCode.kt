package kr.co.fitview.api.app.global.exception.error.request

import kr.co.fitview.api.app.global.dto.ApiResponse
import kr.co.fitview.api.app.global.exception.error.ErrorCode
import org.springframework.http.HttpStatus

enum class RequestErrorCode (
    override val rawCode: String,
    override val message: String,
    override val description: String
) : ErrorCode {

    REQ_FIELD_NOT_VALID("001", "Required request field is not valid.", "요청 필드에 값을 넣어야하는데 안넣거나 요청 필드명이 잘못됨"),
    REQ_HEADER_NOT_VALID("002", "Required request header is not valid.", "헤더값 요청을 받아야하는데 요청을 안주거나 형식에 맞지 않음"),
    REQ_ENUM_MISMATCH("003", "The provided value does not match any valid enum.", "요청 필드값이 정해진 형식값이 있는데 해당 형식을 맞추지 않음")

    ;

    override val prefix: String
        get() = "REQ"

    override fun toApiResponse(): ApiResponse<Any?> =
        ApiResponse.error(HttpStatus.BAD_REQUEST, code, message)
}