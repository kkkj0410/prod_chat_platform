package kr.co.fitview.api.app.domain.chat.dto.response

import kr.co.fitview.api.app.global.dto.ApiResponse
import kr.co.fitview.api.app.global.dto.CursorAtPagination
import kr.co.fitview.api.app.global.dto.SuccessCursorAtPagedResponse
import org.springframework.data.domain.Slice
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

data class SuccessCursorAtPagedResponseByChatMessage<T, U>(
    val content: List<T>,
    val otherMember: U,
    val pagination: CursorAtPagination
){
    companion object {
        fun <T, U> fromSlice(
            slice: Slice<T>,
            otherMember: U,
            timeExtractor: (T) -> LocalDateTime
        ): SuccessCursorAtPagedResponseByChatMessage<T, U> {
            val pagination = CursorAtPagination.from(slice, timeExtractor)
            return SuccessCursorAtPagedResponseByChatMessage(
                content = slice.content,
                otherMember = otherMember,
                pagination = pagination
            )
        }

        fun <T, U> from(
            slice: Slice<T>,
            otherMember: U,
            timeExtractor: (T) -> LocalDateTime
        ): ApiResponse<SuccessCursorAtPagedResponseByChatMessage<T, U>> {
            return ApiResponse(
                status = HttpStatus.OK.value(),
                code = "ok",
                message = HttpStatus.OK.reasonPhrase,
                data = fromSlice(slice, otherMember, timeExtractor)
            )
        }
    }
}

