package kr.co.fitview.api.app.domain.review.controller

import jakarta.validation.Valid
import kr.co.fitview.api.app.domain.review.dto.request.ReviewCreateRequest
import kr.co.fitview.api.app.domain.review.dto.response.ReviewCategoryResponse
import kr.co.fitview.api.app.domain.review.dto.response.ReviewCreateResponse
import kr.co.fitview.api.app.domain.review.service.ReviewService
import kr.co.fitview.api.app.domain.review.service.ReviewTagCountQueryService
import kr.co.fitview.api.app.global.dto.ApiResponse
import kr.co.fitview.api.app.global.util.SecurityUtil
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/v1/reviews")
class ReviewController(
    private val reviewService : ReviewService,
    private val securityUtil : SecurityUtil
) {

    @GetMapping("/tags")
    fun reviewTagList(

    ): ResponseEntity<ApiResponse<List<ReviewCategoryResponse>>>
    {
        val response = reviewService.findReviewCategoryAndTag()

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @PostMapping("")
    fun reviewAdd(
        @Valid
        @RequestBody
        request : ReviewCreateRequest

    ): ResponseEntity<ApiResponse<ReviewCreateResponse>>
    {
        val savedReview = reviewService.saveReview(securityUtil.getMemberId(), request.toServiceRequest())

        val response = ReviewCreateResponse(
            toMemberId = savedReview.getToMemberId()
        )

        return ResponseEntity.ok(ApiResponse.success(response))
    }
}