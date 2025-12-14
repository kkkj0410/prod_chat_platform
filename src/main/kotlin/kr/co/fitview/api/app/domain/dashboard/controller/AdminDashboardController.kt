package kr.co.fitview.api.app.domain.dashboard.controller

import jakarta.validation.Valid
import kr.co.fitview.api.app.domain.auth.HeaderClientType
import kr.co.fitview.api.app.domain.auth.constant.AuthConstant
import kr.co.fitview.api.app.domain.auth.dto.request.*
import kr.co.fitview.api.app.domain.auth.dto.response.AccessTokenRefreshResponse
import kr.co.fitview.api.app.domain.auth.dto.response.MemberLoginResponse
import kr.co.fitview.api.app.domain.auth.service.AuthService
import kr.co.fitview.api.app.domain.auth.service.RefreshTokenService
import kr.co.fitview.api.app.domain.auth.service.TestAuthService
import kr.co.fitview.api.app.domain.dashboard.dto.response.AdminDashboardResponse
import kr.co.fitview.api.app.domain.dashboard.dto.response.AdminDashboardToday
import kr.co.fitview.api.app.domain.dashboard.dto.response.AdminDashboardTotal
import kr.co.fitview.api.app.global.dto.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/v1/admins")
class AdminDashboardController(

) {

    @GetMapping("/dashboards")
    fun dashBoardDetail(
    ): ResponseEntity<ApiResponse<AdminDashboardResponse>> {

        val today = AdminDashboardToday(
            memberCount = 5,
            workoutPartnerCount = 2,
            workoutHistoryCount = 12,
            reviewCount = 3,
            reportCount = 1
        )

        val total = AdminDashboardTotal(
            memberCount = 120,
            workoutPartnerCount = 45,
            workoutHistoryCount = 200,
            reviewCount = 50
        )

        val response = AdminDashboardResponse(
            today = today,
            total = total
        )

        return ResponseEntity.ok(ApiResponse.success(response))
    }

}