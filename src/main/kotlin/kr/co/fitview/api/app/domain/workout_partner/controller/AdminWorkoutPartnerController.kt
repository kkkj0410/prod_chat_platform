package kr.co.fitview.api.app.domain.workout_partner.controller

import kr.co.fitview.api.app.domain.workout_partner.condition.AdminWorkoutPartnerRequestCondition
import kr.co.fitview.api.app.domain.workout_partner.dto.response.AdminWorkoutPartnerRequestResponse
import kr.co.fitview.api.app.domain.workout_partner.dto.response.enums.WorkoutPartnerRequestStatusForResponse
import kr.co.fitview.api.app.domain.workout_partner.entity.enums.WorkoutPartnerRequestStatus
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

        return ResponseEntity.ok(
            ApiResponse.successWithCursorPagination(
                slice = response,
                idExtractor = { it.workoutPartnerRequestId },
            )
        )
    }

}