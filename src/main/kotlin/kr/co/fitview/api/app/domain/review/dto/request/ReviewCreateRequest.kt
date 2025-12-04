package kr.co.fitview.api.app.domain.review.dto.request

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.fitview.api.app.domain.review.dto.request.enums.ReviewRequestType

data class ReviewCreateRequest(

    @field:NotNull(message = "workoutHistoryId is required")
    val workoutHistoryId : Long?,

    @field:NotNull(message = "type is required")
    val type : ReviewRequestType?,

    @field:Size(min = 1, message = "reviewTagIds cannot be empty")
    val reviewTagIds : List<String>?,

    val content : String?
){

    fun toServiceRequest() : ReviewCreateServiceRequest {
        return ReviewCreateServiceRequest(
            workoutHistoryId = workoutHistoryId!!,
            type = type!!,
            reviewTagIds = reviewTagIds,
            content = content
        )
    }

}
