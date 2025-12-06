package kr.co.fitview.api.app.domain.workout_history.controller

import jakarta.validation.Valid
import kr.co.fitview.api.app.domain.review.dto.request.ReviewCreateRequest
import kr.co.fitview.api.app.domain.review.dto.response.ReviewCategoryResponse
import kr.co.fitview.api.app.domain.review.service.ReviewService
import kr.co.fitview.api.app.domain.workout_history.dto.response.WorkoutHistoryReviewStatusResponse
import kr.co.fitview.api.app.domain.workout_history.service.WorkoutHistoryQueryService
import kr.co.fitview.api.app.domain.workout_history.service.WorkoutHistoryService
import kr.co.fitview.api.app.global.dto.ApiResponse
import kr.co.fitview.api.app.global.util.SecurityUtil
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/v1/workout-histories")
class WorkoutHistoryController(
    private val workoutHistoryQueryService : WorkoutHistoryQueryService,
    private val securityUtil : SecurityUtil
) {

    @GetMapping("/{workoutHistoryId}/reviews/statuses")
    fun workoutHistoryReviewStatus(
        @PathVariable
        workoutHistoryId : Long

    ): ResponseEntity<ApiResponse<WorkoutHistoryReviewStatusResponse>>
    {
        val response = workoutHistoryQueryService.findWorkoutHistoryReviewStatus(securityUtil.getMemberId(), workoutHistoryId)

        return ResponseEntity.ok(ApiResponse.success(response))
    }

}