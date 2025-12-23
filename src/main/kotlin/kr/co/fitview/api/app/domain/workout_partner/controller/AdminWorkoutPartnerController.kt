package kr.co.fitview.api.app.domain.workout_partner.controller

import kr.co.fitview.api.app.domain.workout_partner.condition.AdminWorkoutPartnerRequestCondition
import kr.co.fitview.api.app.domain.workout_partner.dto.response.AdminWorkoutPartnerRequestResponse
import kr.co.fitview.api.app.domain.workout_partner.dto.response.enums.WorkoutPartnerRequestStatusForResponse
import kr.co.fitview.api.app.domain.workout_partner.service.WorkoutPartnerRequestQueryService
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
class AdminWorkoutPartnerController(
    private val workoutPartnerRequestQueryService : WorkoutPartnerRequestQueryService
) {

    @GetMapping("/workout-partner-requests")
    fun workoutPartnerRequestList(
        @ModelAttribute
        condition : AdminWorkoutPartnerRequestCondition,
        ) : ResponseEntity<ApiResponse<SuccessCursorPagedResponse<AdminWorkoutPartnerRequestResponse>>> {

        val response = workoutPartnerRequestQueryService.findAllWorkoutPartnerRequestFrom(condition)

        val allRequests = (1..100).map { i ->
            AdminWorkoutPartnerRequestResponse(
                workoutPartnerRequestId = i.toLong(),
                fromMemberNickname = "FromUser$i",
                toMemberNickname = "ToUser$i",
                workoutPartnerRequestStatus = WorkoutPartnerRequestStatusForResponse.PENDING,
                requestedAt = LocalDateTime.now().minusDays((20 - i).toLong()),
                respondedAt = if (i % 2 == 0) LocalDateTime.now().minusDays((20 - i - 1).toLong()) else null,
                hasChatRoom = i % 2 == 0,
                workoutHistoryCount = (i * 2).toLong()
            )
        }.sortedByDescending { it.workoutPartnerRequestId } // 최신순 정렬

        // lastWorkoutPartnerRequestId 적용 + size만큼 slice (ID < lastId)
        val filteredRequests = allRequests
            .filter { condition.workoutPartnerRequestId?.let { lastId -> it.workoutPartnerRequestId < lastId } ?: true }
            .take(condition.size)

        val filteredRequestsWithExtra = allRequests
            .filter {
                condition.workoutPartnerRequestId
                    ?.let { lastId -> it.workoutPartnerRequestId < lastId }
                    ?: true
            }
            .take(condition.size + 1)

        val hasNext = filteredRequestsWithExtra.size > condition.size

        // Slice 생성
        val slice: Slice<AdminWorkoutPartnerRequestResponse> = SliceImpl(
            filteredRequests,
            PageRequest.of(0, condition.size),
            hasNext
        )

        return ResponseEntity.ok(
            ApiResponse.successWithCursorPagination(
                slice = slice,
                idExtractor = { it.workoutPartnerRequestId },
            )
        )
    }

}