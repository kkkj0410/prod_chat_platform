package kr.co.fitview.api.app.domain.oauth2.controller

import jakarta.validation.Valid
import kr.co.fitview.api.app.domain.member.dto.response.MemberMeResponse
import kr.co.fitview.api.app.domain.oauth2.dto.request.AppleLoginRequest
import kr.co.fitview.api.app.domain.oauth2.dto.response.OAuth2LoginResponse
import kr.co.fitview.api.app.global.dto.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/v1/oauth2")
class OAuth2Controller(
//    val oAuth2Service : OAuth2Service
) {


//    @PostMapping("/apple")
//    fun appleLogin(
//        @RequestParam
//        authCode : String
//    ) : ResponseEntity<ApiResponse<OAuth2LoginResponse>> {
//        val response = oAuth2Service.loginApple(authCode)
//        return ResponseEntity.ok(ApiResponse.success(response))
//    }

}