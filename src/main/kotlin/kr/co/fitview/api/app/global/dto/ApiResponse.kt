package kr.co.fitview.api.app.global.dto

import org.springframework.data.domain.Page
import org.springframework.data.domain.Slice
import org.springframework.http.HttpStatus
import java.time.LocalDateTime
import java.time.ZoneId

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

        fun <T> successWithCursorPagination(
            slice: Slice<T>,
            idExtractor: (T) -> Long,
        ): ApiResponse<SuccessCursorPagedResponse<T>> {
            val pagination = CursorPagination.from(slice, idExtractor)
            return ApiResponse(
                HttpStatus.OK.value(),
                "ok",
                HttpStatus.OK.reasonPhrase,
                SuccessCursorPagedResponse(slice.content, pagination)
            )
        }

        fun <T> successWithCursorAtPagination(
            slice: Slice<T>,
            timeExtractor: (T) -> LocalDateTime,
        ): ApiResponse<SuccessCursorAtPagedResponse<T>> {
            val pagination = CursorAtPagination.from(slice, timeExtractor)

            return ApiResponse(
                status = HttpStatus.OK.value(),
                code = "ok",
                message = HttpStatus.OK.reasonPhrase,
                data = SuccessCursorAtPagedResponse(
                    content = slice.content,
                    pagination = pagination
                )
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

data class SuccessCursorPagedResponse<T>(
    val content: List<T>,
    val pagination: CursorPagination
)

data class CursorPagination(
    val size: Int,
    val cursorId: Long?,
    val hasNext: Boolean
) {

    companion object {
        fun <T> from(
            slice: Slice<T>,
            idExtractor: (T) -> Long,
        ) = CursorPagination(
            size = slice.size,
            cursorId = slice.content.lastOrNull()?.let{idExtractor(it)},
            hasNext = slice.hasNext()
        )
    }
}



data class SuccessCursorAtPagedResponse<T>(
    val content: List<T>,
    val pagination: CursorAtPagination // 시간 기반 Pagination DTO와 명확히 연결
)

data class CursorAtPagination(
    val size: Int,
    val cursorAt: Long?,
    val hasNext: Boolean
) {

    companion object {
        fun <T> from(
            slice: Slice<T>,
            timeExtractor: (T) -> LocalDateTime,
        ) = CursorAtPagination(
            size = slice.size,
            cursorAt = slice.content.lastOrNull()
                ?.let {
                    timeExtractor(it)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()
                },
            hasNext = slice.hasNext()
        )
    }
}