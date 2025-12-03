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
//        val response = reviewService.findReviewCategoryAndTag()

        val response = listOf(
            ReviewCategoryResponse(
                reviewCategoryId = 100L,
                reviewCategoryDisplayText = "운동 태도",
                tags = listOf(
                    ReviewTagResponse(reviewTagId = 101L, reviewTagDisplayText = "집중력이 좋아요"),
                    ReviewTagResponse(reviewTagId = 102L, reviewTagDisplayText = "루틴이 탄탄해요"),
                    ReviewTagResponse(reviewTagId = 103L, reviewTagDisplayText = "운동 템포가 잘 맞아요"),
                    ReviewTagResponse(reviewTagId = 104L, reviewTagDisplayText = "성실하게 임해요"),
                    ReviewTagResponse(reviewTagId = 105L, reviewTagDisplayText = "페이스가 조금 달랐어요")
                )
            ),

            // 2. <매너/커뮤니케이션> (ID: 200)
            ReviewCategoryResponse(
                reviewCategoryId = 200L,
                reviewCategoryDisplayText = "매너/커뮤니케이션",
                tags = listOf(
                    ReviewTagResponse(reviewTagId = 201L, reviewTagDisplayText = "운동 자세를 서로 존중해요"),
                    ReviewTagResponse(reviewTagId = 202L, reviewTagDisplayText = "시간 약속을 잘 지켜요"),
                    ReviewTagResponse(reviewTagId = 203L, reviewTagDisplayText = "피드백이 깔끔해요"),
                    ReviewTagResponse(reviewTagId = 204L, reviewTagDisplayText = "집중 흐름을 잘 맞춰줘요"),
                    ReviewTagResponse(reviewTagId = 205L, reviewTagDisplayText = "기구 사용 순서를 배려해요")
                )
            ),

            // 3. <에너지/분위기> (ID: 300)
            ReviewCategoryResponse(
                reviewCategoryId = 300L,
                reviewCategoryDisplayText = "에너지/분위기",
                tags = listOf(
                    ReviewTagResponse(reviewTagId = 301L, reviewTagDisplayText = "긍정적인 에너지를 주는 분이에요"),
                    ReviewTagResponse(reviewTagId = 302L, reviewTagDisplayText = "운동 분위기를 잘 이끌어요"),
                    ReviewTagResponse(reviewTagId = 303L, reviewTagDisplayText = "동기부여가 돼요"),
                    ReviewTagResponse(reviewTagId = 304L, reviewTagDisplayText = "운동 중 불필요한 방해가 없어요"),
                    ReviewTagResponse(reviewTagId = 305L, reviewTagDisplayText = "웃으면서 즐겁게 운동했어요")
                )
            ),

            // 4. <운동 스타일> (ID: 400)
            ReviewCategoryResponse(
                reviewCategoryId = 400L,
                reviewCategoryDisplayText = "운동 스타일",
                tags = listOf(
                    ReviewTagResponse(reviewTagId = 401L, reviewTagDisplayText = "서로 배울 점이 많았어요"),
                    ReviewTagResponse(reviewTagId = 402L, reviewTagDisplayText = "효율적이였어요"),
                    ReviewTagResponse(reviewTagId = 403L, reviewTagDisplayText = "함께 운동하니 동기부여가 되었어요"),
                    ReviewTagResponse(reviewTagId = 404L, reviewTagDisplayText = "무게를 많이 칠 수 있어요"),
                    ReviewTagResponse(reviewTagId = 405L, reviewTagDisplayText = "반복 중심이에요")
                )
            )
        )

        return ResponseEntity.ok(ApiResponse.success(response))
    }
}