package kr.co.fitview.api.app.global.dto

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.data.domain.Page
import org.springframework.data.domain.Slice
import org.springframework.http.HttpStatus

data class ApiResponse<T>(
    val status: Int?,

    val code: String?,

    val message: String?,

    val data: T?,
) {

    companion object {
        fun <T> success(status: HttpStatus, data: T): ApiResponse<T> {
            return ApiResponse(status.value(), null, status.reasonPhrase, data)
        }

        fun <T> successWithPagination(data: List<T>, pagination: Pagination): ApiResponse<SuccessPagedResponse<T>> {
            return ApiResponse(
                HttpStatus.OK.value(),
                null,
                HttpStatus.OK.reasonPhrase,
                SuccessPagedResponse(data, pagination)
            )
        }

        fun <T> fail(status: HttpStatus, code: String, message: String): ApiResponse<T> {
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
            page.number,
            page.size,
            page.totalElements,
            page.totalPages,
            page.hasNext(),
            page.hasPrevious()
        )

        fun from(slice: Slice<*>) = Pagination(
            slice.number,
            slice.size,
            null,
            null,
            slice.hasNext(),
            slice.hasPrevious()
        )
    }
}