package kr.co.fitview.api.app.domain.image.dto.request

import jakarta.validation.constraints.NotNull

data class WorkoutImageUrlServiceRequest(

    val imageUrl : String,
    val sequence : Int
) {

}