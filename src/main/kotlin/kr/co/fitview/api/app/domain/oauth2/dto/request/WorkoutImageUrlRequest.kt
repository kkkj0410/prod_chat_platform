package kr.co.fitview.api.app.domain.oauth2.dto.request

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import kr.co.fitview.api.app.domain.image.dto.request.WorkoutImageUrlServiceRequest

data class WorkoutImageUrlRequest(

    @field:NotNull(message = "imageUrl is required")
    val imageUrl : String?,

    @field:NotNull(message = "sequence is required")
    @field:Min(value = 0, message = "sequence must be 0 or positive")
    val sequence : Int?
) {


    fun toServiceRequest() : WorkoutImageUrlServiceRequest{
        return WorkoutImageUrlServiceRequest(
            imageUrl = imageUrl!!,
            sequence = sequence!!
        )
    }
}