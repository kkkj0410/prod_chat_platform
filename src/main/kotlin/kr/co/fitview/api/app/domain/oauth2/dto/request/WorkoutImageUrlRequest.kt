package kr.co.fitview.api.app.domain.oauth2.dto.request

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import kr.co.fitview.api.app.domain.image.dto.request.WorkoutImageUrlServiceRequest
import kr.co.fitview.api.app.domain.term.dto.request.TermServiceRequest

data class WorkoutImageUrlRequest(

    @field:NotNull(message = "imageUrl is required")
    val imageUrl : String?,

) {


    fun toServiceRequest() : WorkoutImageUrlServiceRequest{
        return WorkoutImageUrlServiceRequest(
            imageUrl = imageUrl!!,
        )
    }

    companion object {
        fun toServiceRequest(requests: List<WorkoutImageUrlRequest>): List<WorkoutImageUrlServiceRequest> {
            return requests.map { it.toServiceRequest()}
        }
    }
}