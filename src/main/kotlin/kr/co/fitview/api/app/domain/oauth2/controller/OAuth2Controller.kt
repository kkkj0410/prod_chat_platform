package kr.co.fitview.api.app.domain.oauth2.controller

import jakarta.validation.Valid

import kr.co.fitview.api.app.domain.oauth2.dto.request.OAuth2LoginRequest
import kr.co.fitview.api.app.domain.oauth2.dto.response.OAuth2LoginResponse

import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.global.dto.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/v1/oauth2")
class OAuth2Controller(
    val oAuth2Service: OAuth2Service
) {

//
//    @PostMapping("/apple")
//    fun appleLogin(
//        @Valid
//        @RequestBody
//        request : AppleLoginRequest
//    ) : ResponseEntity<ApiResponse<OAuth2LoginResponse>> {
//        val response = appleService.loginAppleWithSignup(request.toServiceRequest())
//
//        return ResponseEntity.ok(ApiResponse.success(response))
//    }
//
//    @PostMapping("/kakao")
//    fun kakaoLogin(
//        @Valid
//        @RequestBody
//        request : KakaoLoginRequest
//    ) : ResponseEntity<ApiResponse<OAuth2LoginResponse>> {
//        val response = kakaoService.loginKakaoWithSignup(request.toServiceRequest())
//
//        return ResponseEntity.ok(ApiResponse.success(response))
//    }


    @PostMapping("/login")
    fun oAuth2Login(
        @Valid
        @RequestBody
        request : OAuth2LoginRequest
    ) : ResponseEntity<ApiResponse<OAuth2LoginResponse>> {

        val response = oAuth2Service.loginWithAdd(request.toServiceRequest())

        return ResponseEntity.ok(ApiResponse.success(response))
    }


}