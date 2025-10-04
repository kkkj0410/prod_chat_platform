package kr.co.fitview.api.app.global.exception

import kr.co.fitview.api.app.domain.auth.HeaderClientType
import kr.co.fitview.api.app.global.dto.ApiResponse
import kr.co.fitview.api.app.global.exception.error.request.RequestErrorCode
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingRequestHeaderException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(GlobalException::class)
    fun handleGlobalException(ex: GlobalException): ResponseEntity<ApiResponse<Any?>> {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ex.errorCode.toApiResponse())
    }


    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(ex: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Any?>> {
        val errorMessage = ex.bindingResult.fieldErrors.joinToString(", ") { error ->
            "${error.defaultMessage}"
        }

        val response = ApiResponse.error(
            status = HttpStatus.BAD_REQUEST,
            code = RequestErrorCode.REQ_FIELD_NOT_VALID.code,
            message = errorMessage
        )

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(response)
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleEnumMismatch(ex: MethodArgumentTypeMismatchException): ResponseEntity<ApiResponse<*>> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(RequestErrorCode.REQ_ENUM_MISMATCH.toApiResponse())
    }

    @ExceptionHandler(MissingRequestHeaderException::class)
    fun handleMissingHeader(ex: MissingRequestHeaderException): ResponseEntity<ApiResponse<*>> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(RequestErrorCode.REQ_HEADER_NOT_VALID.toApiResponse())
    }

}