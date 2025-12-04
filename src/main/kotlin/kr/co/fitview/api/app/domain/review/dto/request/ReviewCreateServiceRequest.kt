package kr.co.fitview.api.app.domain.review.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.fitview.api.app.domain.oauth2.dto.request.AppleLoginServiceRequest
import kr.co.fitview.api.app.domain.review.dto.request.enums.ReviewRequestType
import org.checkerframework.checker.units.qual.min

data class ReviewCreateServiceRequest(

    val workoutHistoryId : Long,
    val type : ReviewRequestType,
    val reviewTagIds : List<String>?,
    val content : String?
){

}
