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
import kr.co.fitview.api.app.domain.dashboard.service.DashboardQueryService
import kr.co.fitview.api.app.global.dto.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/v1/admins")
class AdminDashboardController(
    private val dashboardQueryService: DashboardQueryService
) {

    @GetMapping("/dashboards")
    fun dashBoardDetail(
    ): ResponseEntity<ApiResponse<AdminDashboardResponse>> {

        val response = dashboardQueryService.findDashboardComposite()

        return ResponseEntity.ok(ApiResponse.success(response))
    }

}