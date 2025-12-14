package kr.co.fitview.api.app.domain.review.controller

import jakarta.validation.Valid
import kr.co.fitview.api.app.domain.member.condition.AdminMemberCondition
import kr.co.fitview.api.app.domain.review.condition.AdminReviewCondition
import kr.co.fitview.api.app.domain.review.dto.request.ReviewCreateRequest
import kr.co.fitview.api.app.domain.review.dto.response.AdminReviewResponse
import kr.co.fitview.api.app.domain.review.dto.response.ReviewCategoryResponse
import kr.co.fitview.api.app.domain.review.dto.response.ReviewCreateResponse
import kr.co.fitview.api.app.domain.review.entity.enums.ReviewType
import kr.co.fitview.api.app.domain.review.service.ReviewService
import kr.co.fitview.api.app.domain.review.service.ReviewTagCountQueryService
import kr.co.fitview.api.app.global.dto.ApiResponse
import kr.co.fitview.api.app.global.dto.SuccessCursorPagedResponse
import kr.co.fitview.api.app.global.util.SecurityUtil
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime


@RestController
@RequestMapping("/api/v1/admins/reviews")
class AdminReviewController(
) {

    @GetMapping("")
    fun reviewList(
        @ModelAttribute
        condition : AdminReviewCondition,
    ): ResponseEntity<ApiResponse<SuccessCursorPagedResponse<AdminReviewResponse>>>
    {

        val reviewTypes = ReviewType.entries.toTypedArray()

        // 1. 전체 가짜 데이터 생성
        val allReviews = (1..100).map { i ->
            AdminReviewResponse(
                reviewId = i.toLong(),
                workoutPartnerId = (i % 10 + 1).toLong(),
                workoutHistoryId = (i * 10).toLong(),
                fromMemberNickname = "FromUser$i",
                toMemberNickname = "ToUser$i",
                reviewType = reviewTypes.random(),
                reviewTagDisplayTexts = listOf(
                    "친절해요",
                    "시간 약속을 잘 지켜요",
                    "운동 설명이 좋아요"
                ).shuffled().take(2),
                reviewContent = if (i % 2 == 0) "아주 만족스러운 운동이었습니다." else null,
                postedAt = LocalDateTime.now().minusDays(i.toLong())
            )
        }.sortedByDescending { it.reviewId } // 최신순

        // 2. 커서 조건 적용 (reviewId 기준)
        val filteredReviews = allReviews
            .filter {
                condition.reviewId?.let { lastId ->
                    it.reviewId < lastId
                } ?: true
            }
            .take(condition.size)

        // 3. Slice 생성
        val slice: Slice<AdminReviewResponse> = SliceImpl(
            filteredReviews,
            PageRequest.of(0, condition.size),
            allReviews.size > filteredReviews.size
        )

        // 4. Cursor 기반 응답
        return ResponseEntity.ok(
            ApiResponse.successWithCursorPagination(
                slice = slice,
                idExtractor = { it.reviewId }
            )
        )
    }
}