package kr.co.fitview.api.app.global.dto

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.data.domain.Page
import org.springframework.data.domain.Slice
import org.springframework.http.HttpStatus

data class ApiResponse<T>(
    val status: Int,

    val code: String,

    val message: String,

    val data: T,
) {

    companion object {
        fun <T> success(data: T): ApiResponse<T> {
            return ApiResponse(HttpStatus.OK.value(), "ok", "ok", data)
        }

        fun <T> successWithPagination(page: Page<T>): ApiResponse<SuccessPagedResponse<T>> {
            val pagination = Pagination.from(page) // Page → Pagination
            return ApiResponse(
                HttpStatus.OK.value(),
                "ok",
                HttpStatus.OK.reasonPhrase,
                SuccessPagedResponse(page.content, pagination)
            )
        }

        fun <T> successWithPagination(slice: Slice<T>): ApiResponse<SuccessPagedResponse<T>> {
            val pagination = Pagination.from(slice)
            return ApiResponse(
                HttpStatus.OK.value(),
                "ok",
                HttpStatus.OK.reasonPhrase,
                SuccessPagedResponse(slice.content, pagination)
            )
        }

        fun error(status: HttpStatus, code: String, message: String): ApiResponse<Any?> {
            return ApiResponse(status.value(), code, message, null)
        }
    }
}


data class SuccessPagedResponse<T>(
    val content: List<T>,
    val pagination: Pagination
)


data class Pagination(
    val page: Int,
    val size: Int,
    val totalElements: Long?,
    val totalPages: Int?,
    val hasNext: Boolean,
    val hasPrevious: Boolean
) {
    companion object {
        fun from(page: Page<*>) = Pagination(
            page.number + 1,
            page.size,
            page.totalElements,
            page.totalPages,
            page.hasNext(),
            page.hasPrevious()
        )

        fun from(slice: Slice<*>) = Pagination(
            slice.number + 1,
            slice.size,
            null,
            null,
            slice.hasNext(),
            slice.hasPrevious()
        )
    }
}