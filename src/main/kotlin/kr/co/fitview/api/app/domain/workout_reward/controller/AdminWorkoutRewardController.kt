package kr.co.fitview.api.app.domain.workout_reward.controller

import jakarta.validation.Valid
import kr.co.fitview.api.app.domain.workout_reward.condition.AdminWorkoutRewardCondition
import kr.co.fitview.api.app.domain.workout_reward.dto.request.AdminWorkoutRewardCouponStatusRequest
import kr.co.fitview.api.app.domain.workout_reward.dto.response.AdminWorkoutRewardClaimResponse
import kr.co.fitview.api.app.domain.workout_reward.entity.enums.WorkoutRewardClaimCouponStatus
import kr.co.fitview.api.app.domain.workout_reward.service.WorkoutRewardQueryService
import kr.co.fitview.api.app.domain.workout_reward.service.WorkoutRewardService
import kr.co.fitview.api.app.global.dto.ApiResponse
import kr.co.fitview.api.app.global.dto.SuccessCursorAtPagedResponse
import kr.co.fitview.api.app.global.util.SecurityUtil
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.SliceImpl
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.random.Random


@RestController
@RequestMapping("/api/v1/admins/workout-rewards")
class AdminWorkoutRewardController(
    private val workoutRewardService : WorkoutRewardService,
    private val workoutRewardQueryService: WorkoutRewardQueryService,
    private val securityUtil : SecurityUtil
) {

    @GetMapping("")
    fun workoutRewardList(
        @ModelAttribute
        condition: AdminWorkoutRewardCondition,
    ): ResponseEntity<ApiResponse<SuccessCursorAtPagedResponse<AdminWorkoutRewardClaimResponse>>> {

        val response = workoutRewardQueryService.findAllWorkoutReward(condition)

        return ResponseEntity.ok(
            ApiResponse.successWithCursorAtPagination(response) { it.createdAt }
        )
    }

    @PostMapping("/claims/{workoutRewardClaimId}/coupon-status")
    fun updateWorkoutRewardCouponStatus(
        @PathVariable workoutRewardClaimId: Long,

        @RequestBody
        @Valid
        request: AdminWorkoutRewardCouponStatusRequest
    ): ResponseEntity<ApiResponse<String>> {


        return ResponseEntity.ok(ApiResponse.success("ok"))
    }

}