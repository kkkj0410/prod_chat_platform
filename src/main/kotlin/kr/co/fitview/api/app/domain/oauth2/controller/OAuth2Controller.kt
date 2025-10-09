package kr.co.fitview.api.app.domain.oauth2.controller

import kr.co.fitview.api.app.domain.oauth2.dto.request.AppleLoginRequest
import kr.co.fitview.api.app.domain.oauth2.dto.response.OAuth2LoginResponse
import kr.co.fitview.api.app.domain.oauth2.service.AppleService
import kr.co.fitview.api.app.global.dto.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/v1/oauth2")
class OAuth2Controller(
    val appleService : AppleService
) {


    @PostMapping("/apple")
    fun appleLogin(
        @RequestBody
        request : AppleLoginRequest
    ) : ResponseEntity<ApiResponse<OAuth2LoginResponse>> {
        val response = appleService.loginAppleWithSignup(request)

        return ResponseEntity.ok(ApiResponse.success(response))
    }

}