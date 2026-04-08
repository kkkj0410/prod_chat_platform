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

        val size = condition.size

        // 1. Long? 타임스탬프를 LocalDateTime으로 변환 (null이면 현재 시간)
        val baseTime = if (condition.cursorAt != null) {
            LocalDateTime.ofInstant(Instant.ofEpochMilli(condition.cursorAt), ZoneId.systemDefault())
        } else {
            LocalDateTime.now()
        }

        val couponNames = listOf("[스타벅스] 5,000원", "[이마트] 10,000원", "[배달의민족] 5,000원", "[GS25] 10,000원")
        val stampLevels = listOf("3회", "5회")

        // 2. 변환된 baseTime(LocalDateTime)을 사용하여 데이터 생성
        val dummyContent = List(size) { index ->
            val isPending = Random.nextBoolean()

            AdminWorkoutRewardClaimResponse(
                workoutRewardClaimId = Random.nextLong(1, 10000),
                nickname = "핏뷰유저${Random.nextInt(1000, 9999)}",
                stampLevelDisplayName = stampLevels.random(),
                couponName = couponNames.random(),
                phoneNumber = "010${Random.nextInt(10000000, 99999999)}",
                // 이제 LocalDateTime이므로 .minusHours()가 잘 작동합니다!
                createdAt = baseTime.minusHours(index.toLong() + 1),
                coupon = AdminWorkoutRewardClaimResponse.CouponStatusInfo(
                    status = if (isPending) WorkoutRewardClaimCouponStatus.PENDING else WorkoutRewardClaimCouponStatus.ISSUED,
                    statusLabel = if (isPending) WorkoutRewardClaimCouponStatus.PENDING.displayName else WorkoutRewardClaimCouponStatus.ISSUED.displayName
                )
            )
        }

        val hasNext = Random.nextBoolean()
        val slice = SliceImpl(dummyContent, PageRequest.of(0, size), hasNext)

        return ResponseEntity.ok(
            ApiResponse.successWithCursorAtPagination(slice) { it.createdAt }
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