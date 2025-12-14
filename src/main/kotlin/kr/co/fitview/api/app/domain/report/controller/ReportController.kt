package kr.co.fitview.api.app.domain.report.controller

import jakarta.validation.Valid

import kr.co.fitview.api.app.domain.oauth2.dto.request.OAuth2LoginRequest
import kr.co.fitview.api.app.domain.oauth2.dto.request.OAuth2SignupRequest
import kr.co.fitview.api.app.domain.oauth2.dto.response.OAuth2LoginResponse

import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.domain.report.dto.request.ReportChatRoomCreateRequest
import kr.co.fitview.api.app.domain.report.dto.request.ReportMemberCreateRequest
import kr.co.fitview.api.app.global.dto.ApiResponse
import kr.co.fitview.api.app.global.util.SecurityUtil
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/v1/reports")
class ReportController(
    val securityUtil : SecurityUtil
) {


    @PostMapping("/chats")
    fun reportChatRoomAdd(
        @Valid
        @RequestBody
        request : ReportChatRoomCreateRequest
    ) : ResponseEntity<ApiResponse<*>> {


        return ResponseEntity.ok(ApiResponse.success("ok"))
    }


    @PostMapping("/members")
    fun reportMemberAdd(
        @Valid
        @RequestBody
        request : ReportMemberCreateRequest
    ) : ResponseEntity<ApiResponse<*>> {


        return ResponseEntity.ok(ApiResponse.success("ok"))
    }


}