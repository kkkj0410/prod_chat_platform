package kr.co.fitview.api.app.domain.auth.controller

import jakarta.validation.Valid
import kr.co.fitview.api.app.domain.auth.HeaderClientType
import kr.co.fitview.api.app.domain.auth.constant.AuthConstant
import kr.co.fitview.api.app.domain.auth.dto.request.MemberCreateRequest
import kr.co.fitview.api.app.domain.auth.dto.request.MemberLoginRequest
import kr.co.fitview.api.app.domain.auth.dto.request.AccessTokenRefreshRequest
import kr.co.fitview.api.app.domain.auth.dto.request.MemberLogoutRequest
import kr.co.fitview.api.app.domain.auth.dto.response.AccessTokenRefreshResponse
import kr.co.fitview.api.app.domain.auth.dto.response.MemberLoginResponse
import kr.co.fitview.api.app.domain.auth.service.AuthService
import kr.co.fitview.api.app.domain.auth.service.RefreshTokenService
import kr.co.fitview.api.app.global.dto.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    val authService : AuthService,
    val refreshTokenService : RefreshTokenService
) {

    @PostMapping("/signup")
    fun memberSave(
        @Valid
        @RequestBody
        request: MemberCreateRequest
    ): ResponseEntity<ApiResponse<*>> {
        authService.signup(request.toServiceRequest())
        return ResponseEntity.ok(ApiResponse.success("ok"))
    }

    @PostMapping("/login")
    fun memberLogin(
        @RequestHeader(AuthConstant.HEADER_CLIENT_TYPE)
        headerClientType : HeaderClientType,

        @Valid
        @RequestBody
        request: MemberLoginRequest

    ): ResponseEntity<ApiResponse<MemberLoginResponse>> {
        val response = authService.login(request.toServiceRequest(), headerClientType)

        if(isMobile(headerClientType)){
            return ResponseEntity.ok(ApiResponse.success(response))
        }

        return ResponseEntity.ok()
            .headers(response.refreshTokenCookieHeader)
            .body(ApiResponse.success(response))
    }

    @PostMapping("/logout")
    fun memberLogout(
        @Valid
        @RequestBody
        request: MemberLogoutRequest
    ): ResponseEntity<ApiResponse<String>> {
        refreshTokenService.inactiveRefreshToken(request.refreshToken!!)

        return ResponseEntity.ok(ApiResponse.success("ok"))
    }

    @PostMapping("/refresh")
    fun accessTokenRefresh(
        @Valid
        @RequestBody
        request: AccessTokenRefreshRequest
    ): ResponseEntity<ApiResponse<AccessTokenRefreshResponse>> {
        val response = authService.refreshAccessToken(request.toServiceRequest())

        return ResponseEntity.ok(ApiResponse.success(response))
    }



    private fun isMobile(headerClientType: HeaderClientType) =
        headerClientType == HeaderClientType.MOBILE

}