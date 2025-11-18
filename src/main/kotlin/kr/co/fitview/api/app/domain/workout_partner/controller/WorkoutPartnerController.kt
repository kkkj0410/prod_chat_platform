package kr.co.fitview.api.app.domain.workout_partner.controller

import jakarta.validation.Valid
import kr.co.fitview.api.app.domain.workout_partner.dto.request.WorkoutPartnerCreateRequest
import kr.co.fitview.api.app.domain.workout_partner.dto.request.WorkoutPartnerUpdateRequest
import kr.co.fitview.api.app.domain.workout_partner.service.WorkoutPartnerService
import kr.co.fitview.api.app.global.dto.ApiResponse
import kr.co.fitview.api.app.global.util.SecurityUtil
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/v1")
class WorkoutPartnerController(
    private val workoutPartnerService : WorkoutPartnerService,
    private val securityUtil : SecurityUtil
) {

    @PostMapping("/workout-partners")
    fun workoutPartnerAdd(
        @Valid
        @RequestBody
        request : WorkoutPartnerCreateRequest
    ) : ResponseEntity<ApiResponse<*>> {

        workoutPartnerService.addWorkoutPartnerRequest(securityUtil.getMemberId(), request.toServiceRequest())

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

        workoutPartnerService.updateWorkoutPartnerRequest(securityUtil.getMemberId(), workoutPartnerRequestId, request.toServiceRequest())

        return ResponseEntity.ok(ApiResponse.success("ok"))
    }

}