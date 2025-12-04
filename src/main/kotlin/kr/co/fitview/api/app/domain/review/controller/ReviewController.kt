package kr.co.fitview.api.app.domain.review.controller

import jakarta.validation.Valid
import kr.co.fitview.api.app.domain.image.dto.request.S3UploadUrlRequest
import kr.co.fitview.api.app.domain.image.dto.response.S3UploadUrlResponse
import kr.co.fitview.api.app.domain.image.service.S3Service
import kr.co.fitview.api.app.domain.review.dto.response.ReviewCategoryResponse
import kr.co.fitview.api.app.domain.review.dto.response.ReviewTagResponse
import kr.co.fitview.api.app.domain.review.service.ReviewService
import kr.co.fitview.api.app.global.dto.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/v1/reviews")
class ReviewController(
    private val reviewService : ReviewService
) {

    @GetMapping("/tags")
    fun reviewTagList(

    ): ResponseEntity<ApiResponse<List<ReviewCategoryResponse>>>
    {
        val response = reviewService.findReviewCategoryAndTag()

        return ResponseEntity.ok(ApiResponse.success(response))
    }
}