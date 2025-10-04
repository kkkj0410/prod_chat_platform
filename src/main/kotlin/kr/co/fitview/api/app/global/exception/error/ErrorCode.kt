package kr.co.fitview.api.app.global.exception.error

import kr.co.fitview.api.app.global.dto.ApiResponse
import org.apache.naming.SelectorContext.prefix
import org.springframework.http.HttpStatus

interface ErrorCode {

    val prefix : String
    val rawCode : String

    val code: String get() = "${prefix}_$rawCode"
    val message: String

    fun toApiResponse(): ApiResponse<Any?> =
        ApiResponse.error(HttpStatus.UNAUTHORIZED, code, message)
}