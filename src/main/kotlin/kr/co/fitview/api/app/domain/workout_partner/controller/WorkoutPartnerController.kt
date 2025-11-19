package kr.co.fitview.api.app.domain.workout_partner.controller

import jakarta.validation.Valid
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle
import kr.co.fitview.api.app.domain.workout_partner.condition.WorkoutPartnerRequestCondition
import kr.co.fitview.api.app.domain.workout_partner.dto.request.WorkoutPartnerCreateRequest
import kr.co.fitview.api.app.domain.workout_partner.dto.request.WorkoutPartnerUpdateRequest
import kr.co.fitview.api.app.domain.workout_partner.dto.response.WorkoutPartnerRequestResponse
import kr.co.fitview.api.app.domain.workout_partner.dto.response.enums.WorkoutPartnerRequestStatusForResponse
import kr.co.fitview.api.app.domain.workout_partner.service.WorkoutPartnerRequestService
import kr.co.fitview.api.app.global.dto.ApiResponse
import kr.co.fitview.api.app.global.dto.SuccessCursorPagedResponse
import kr.co.fitview.api.app.global.util.SecurityUtil
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/v1")
class WorkoutPartnerController(
    private val workoutPartnerRequestService : WorkoutPartnerRequestService,
    private val securityUtil : SecurityUtil
) {

    @PostMapping("/workout-partners")
    fun workoutPartnerAdd(
        @Valid
        @RequestBody
        request : WorkoutPartnerCreateRequest
    ) : ResponseEntity<ApiResponse<*>> {

        workoutPartnerRequestService.addWorkoutPartnerRequest(securityUtil.getMemberId(), request.toServiceRequest())

        return ResponseEntity.ok(ApiResponse.success("ok"))
    }

    @PostMapping("/workout-partners/{workoutPartnerRequestId}")
    fun workoutPartnerModify(
        @PathVariable
        workoutPartnerRequestId : Long,

        @Valid
        @RequestBody
        request : WorkoutPartnerUpdateRequest
    ) : ResponseEntity<ApiResponse<*>> {

        workoutPartnerRequestService.updateWorkoutPartnerRequest(securityUtil.getMemberId(), workoutPartnerRequestId, request.toServiceRequest())

        return ResponseEntity.ok(ApiResponse.success("ok"))
    }

    @GetMapping("/workout-partner-requests")
    fun workoutPartnerRequestList(
        @ModelAttribute
        condition : WorkoutPartnerRequestCondition,
        ) : ResponseEntity<ApiResponse<SuccessCursorPagedResponse<WorkoutPartnerRequestResponse>>> {

        val responseList = listOf(
            WorkoutPartnerRequestResponse(
                workoutPartnerRequestId = 123L,
                profileImageUrl = "https://example.com/profile1.png",
                nickname = "user_one",
                workoutExperience = MemberWorkoutExperience.UNDER_ONE_YEAR,
                workoutStyle = MemberWorkoutStyle.CARDIO,
                workoutGoal = MemberWorkoutGoal.PERFORMANCE_GOAL,
                status = WorkoutPartnerRequestStatusForResponse.PENDING,
                chatRoomId = null
            ),
            WorkoutPartnerRequestResponse(
                workoutPartnerRequestId = 124L,
                profileImageUrl = "https://example.com/profile2.png",
                nickname = "user_two",
                workoutExperience = MemberWorkoutExperience.FOUR_TO_SIX_YEARS,
                workoutStyle = MemberWorkoutStyle.STRENGTH,
                workoutGoal = MemberWorkoutGoal.PERFORMANCE_GOAL,
                status = WorkoutPartnerRequestStatusForResponse.ACCEPT,
                chatRoomId = null
            ),
            WorkoutPartnerRequestResponse(
                workoutPartnerRequestId = 125L,
                profileImageUrl = "https://example.com/profile3.png",
                nickname = "user_three",
                workoutExperience = MemberWorkoutExperience.UNDER_ONE_YEAR,
                workoutStyle = MemberWorkoutStyle.CARDIO,
                workoutGoal = MemberWorkoutGoal.PERFORMANCE_GOAL,
                status = WorkoutPartnerRequestStatusForResponse.EXPIRE,
                chatRoomId = null
            )
        )

        val slice: Slice<WorkoutPartnerRequestResponse> = SliceImpl(responseList)

        return ResponseEntity.ok(
            ApiResponse.successWithCursorPagination(slice) { it.workoutPartnerRequestId }
        )
    }

}