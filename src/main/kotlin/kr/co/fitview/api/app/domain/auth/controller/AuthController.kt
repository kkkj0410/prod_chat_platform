package kr.co.fitview.api.app.domain.auth.controller

import jakarta.validation.Valid
import kr.co.fitview.api.app.domain.auth.HeaderClientType
import kr.co.fitview.api.app.domain.auth.constant.AuthConstant
import kr.co.fitview.api.app.domain.auth.dto.request.*
import kr.co.fitview.api.app.domain.auth.dto.response.AccessTokenRefreshResponse
import kr.co.fitview.api.app.domain.auth.dto.response.MemberLoginResponse
import kr.co.fitview.api.app.domain.auth.service.AuthService
import kr.co.fitview.api.app.domain.auth.service.RefreshTokenService
import kr.co.fitview.api.app.domain.auth.service.TestAuthService
import kr.co.fitview.api.app.global.constant.JwtConstant
import kr.co.fitview.api.app.global.dto.ApiResponse
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.auth.AuthErrorCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    val authService : AuthService,
    val refreshTokenService : RefreshTokenService,
    val testAuthService: TestAuthService
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

    //해당 API는 가짜 데이터를 넣기위한 용도로 사용
    @PostMapping("/signup/test")
    fun memberSaveTest(
        @Valid
        @RequestBody
        request: AuthSignupRequestTest,

        @RequestParam(required = false) minHeight: Int?
    ): ResponseEntity<ApiResponse<*>> {
        testAuthService.signup(request)
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
        @RequestHeader(AuthConstant.HEADER_CLIENT_TYPE, required = false)
        headerClientType : HeaderClientType = HeaderClientType.MOBILE,

        @Valid
        @RequestBody(required = false)
        request: AccessTokenRefreshRequest?,

        @CookieValue(name = JwtConstant.REFRESH_TOKEN_COOKIE_NAME, required = false)
        refreshToken: String?
    ): ResponseEntity<ApiResponse<AccessTokenRefreshResponse>> {

        validateHasRefreshToken(headerClientType, request, refreshToken)

        if(headerClientType == HeaderClientType.MOBILE){
            val response = authService.refreshAccessToken(request!!.toServiceRequest())
            return ResponseEntity.ok(ApiResponse.success(response))
        }

        else{
            val response = authService.refreshAccessToken(AccessTokenRefreshServiceRequest(refreshToken!!))
            return ResponseEntity.ok(ApiResponse.success(response))
        }

    }

    private fun validateHasRefreshToken(
        headerClientType: HeaderClientType,
        request: AccessTokenRefreshRequest?,
        refreshToken: String?
    ) {
        if (headerClientType == HeaderClientType.MOBILE) {
            if (request?.refreshToken == null) {
                throw GlobalException(AuthErrorCode.MOBILE_REFRESH_TOKEN_MISSING)
            }
        } else if (headerClientType == HeaderClientType.WEB) {
            if (refreshToken == null) {
                throw GlobalException(AuthErrorCode.WEB_REFRESH_TOKEN_MISSING)
            }
        }
    }


    private fun isMobile(headerClientType: HeaderClientType) =
        headerClientType == HeaderClientType.MOBILE

}