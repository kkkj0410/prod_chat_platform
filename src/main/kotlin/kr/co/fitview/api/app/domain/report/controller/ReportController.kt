package kr.co.fitview.api.app.domain.report.controller

import jakarta.validation.Valid

import kr.co.fitview.api.app.domain.oauth2.dto.request.OAuth2LoginRequest
import kr.co.fitview.api.app.domain.oauth2.dto.request.OAuth2SignupRequest
import kr.co.fitview.api.app.domain.oauth2.dto.response.OAuth2LoginResponse

import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.domain.report.dto.request.ReportChatRoomCreateRequest
import kr.co.fitview.api.app.domain.report.dto.request.ReportMemberCreateRequest
import kr.co.fitview.api.app.domain.report.dto.response.ReportReasonResponse
import kr.co.fitview.api.app.domain.report.entity.enums.ReportTargetType
import kr.co.fitview.api.app.domain.report.service.ReportReasonQueryService
import kr.co.fitview.api.app.global.dto.ApiResponse
import kr.co.fitview.api.app.global.util.SecurityUtil
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/v1/reports")
class ReportController(
    val reportReasonQueryService: ReportReasonQueryService,
    val securityUtil : SecurityUtil,
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


    @GetMapping("/reasons")
    fun reportReasonList(
        @RequestParam(defaultValue = "MEMBER")
        type: ReportTargetType
    ): ResponseEntity<ApiResponse<List<ReportReasonResponse>>>
    {

//        val response = when (type) {
//            ReportTargetType.MEMBER -> listOf(
//                ReportReasonResponse(100L, "약속시간 미준수 / 노쇼"),
//                ReportReasonResponse(200L, "과도한 개인 정보를 요구"),
//                ReportReasonResponse(300L, "무례한 발언, 성희롱 등 부적절한 언행"),
//                ReportReasonResponse(400L, "허위 정보를 기재"),
//                ReportReasonResponse(500L, "원치 않는 불쾌한 행동"),
//                ReportReasonResponse(600L, "영업을 목적으로 접근"),
//                ReportReasonResponse(700L, "기타")
//            )
//
//            ReportTargetType.CHAT_ROOM -> listOf(
//                // 겹치지 않게 800번부터 시작 (혹은 1100번 등 원하시는 대로)
//                ReportReasonResponse(800L, "불쾌한 언행"),
//                ReportReasonResponse(900L, "협박 또는 위협적인 내용"),
//                ReportReasonResponse(1000L, "약속을 강요하거나 압박"),
//                ReportReasonResponse(1100L, "개인정보를 요구"),
//                ReportReasonResponse(1200L, "부적절한 요청"),
//                ReportReasonResponse(1300L, "사기 또는 금전 요구 의심"),
//                ReportReasonResponse(1400L, "기타")
//            )
//
//            else -> emptyList()
//        }

        val response = reportReasonQueryService.findAllReportReason(type)

        return ResponseEntity.ok(ApiResponse.success(response))

    }


}