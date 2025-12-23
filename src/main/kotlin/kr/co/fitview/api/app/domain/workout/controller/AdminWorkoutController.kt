package kr.co.fitview.api.app.domain.workout.controller

import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import kr.co.fitview.api.app.domain.workout.condition.AdminWorkoutRequestCondition
import kr.co.fitview.api.app.domain.workout.dto.response.AdminDetailReviewResponse
import kr.co.fitview.api.app.domain.workout.dto.response.AdminDetailWorkoutRequestLogResponse
import kr.co.fitview.api.app.domain.workout.dto.response.AdminDetailWorkoutRequestResponse
import kr.co.fitview.api.app.domain.workout.dto.response.AdminWorkoutRequestResponse
import kr.co.fitview.api.app.domain.workout.service.WorkoutRequestQueryService
import kr.co.fitview.api.app.global.dto.ApiResponse
import kr.co.fitview.api.app.global.dto.SuccessCursorPagedResponse
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime


@RestController
@RequestMapping("/api/v1/admins")
class AdminWorkoutController(
    private val workoutRequestQueryService : WorkoutRequestQueryService
) {

    @GetMapping("/workout-requests")
    fun workoutRequestList(
        @ModelAttribute
        condition : AdminWorkoutRequestCondition,
        ): ResponseEntity<ApiResponse<SuccessCursorPagedResponse<AdminWorkoutRequestResponse>>>
    {
        val response = workoutRequestQueryService.findAllWorkoutRequestFrom(condition)

        return ResponseEntity.ok(
            ApiResponse.successWithCursorPagination(
                slice = response,
                idExtractor = { it.workoutRequestId }
            )
        )
    }

    @GetMapping("/workout-requests/{workoutRequestId}")
    fun workoutRequestDetail(
        @PathVariable
        workoutRequestId : Long
    ): ResponseEntity<ApiResponse<AdminDetailWorkoutRequestResponse>>
    {

        val workoutLogs = listOf(
            AdminDetailWorkoutRequestLogResponse(
                workoutRequestStatus = WorkoutRequestStatus.PENDING,
                loggedAt = LocalDateTime.now().minusDays(3),
                fromMemberNickname = "FromUser",
                toMemberNickname = "ToUser"
            ),
            AdminDetailWorkoutRequestLogResponse(
                workoutRequestStatus = WorkoutRequestStatus.ACCEPT,
                loggedAt = LocalDateTime.now().minusDays(2),
                fromMemberNickname = "FromUser",
                toMemberNickname = "ToUser"
            ),
            AdminDetailWorkoutRequestLogResponse(
                workoutRequestStatus = WorkoutRequestStatus.COMPLETE,
                loggedAt = LocalDateTime.now().minusDays(1),
                fromMemberNickname = "FromUser",
                toMemberNickname = "ToUser"
            )
        )

        val reviews = (1..2).map { i ->
            AdminDetailReviewResponse(
                fromMemberNickname = "FromUser",
                toMemberNickname = "ToUser",
                postedAt = LocalDateTime.now().minusDays(2 - i.toLong()) // 1일 전, 2일 전 순
            )
        }.sortedBy { it.postedAt } // 안전하게 정렬

        val response = AdminDetailWorkoutRequestResponse(
            workoutPartnerId = 1L,
            workoutRequestId = workoutRequestId,
            scheduledAt = LocalDateTime.now().plusDays(3),
            location = "서울 강남구 헬스장 101",
            workoutRequestLogs = workoutLogs,
            reviews = reviews
        )

        return ResponseEntity.ok(ApiResponse.success(response))
    }

}