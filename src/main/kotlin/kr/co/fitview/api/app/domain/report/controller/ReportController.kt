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
import kr.co.fitview.api.app.domain.report.service.ChatRoomReportService
import kr.co.fitview.api.app.domain.report.service.MemberReportService
import kr.co.fitview.api.app.domain.report.service.ReportReasonQueryService
import kr.co.fitview.api.app.global.dto.ApiResponse
import kr.co.fitview.api.app.global.util.SecurityUtil
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/v1/reports")
class ReportController(
    private val reportReasonQueryService: ReportReasonQueryService,
    private val chatRoomReportService : ChatRoomReportService,
    private val memberReportService : MemberReportService,
    private val securityUtil : SecurityUtil,
) {


    @PostMapping("/chats")
    fun reportChatRoomAdd(
        @Valid
        @RequestBody
        request : ReportChatRoomCreateRequest
    ) : ResponseEntity<ApiResponse<String>> {

        chatRoomReportService.addChatRoomReport(securityUtil.getMemberId(), request.toServiceRequest())

        return ResponseEntity.ok(ApiResponse.success("ok"))
    }


    @PostMapping("/members")
    fun reportMemberAdd(
        @Valid
        @RequestBody
        request : ReportMemberCreateRequest
    ) : ResponseEntity<ApiResponse<String>> {

        memberReportService.addMemberReport(
            memberId = securityUtil.getMemberId(),
            request = request.toServiceRequest()
        )

        return ResponseEntity.ok(ApiResponse.success("ok"))
    }


    @GetMapping("/reasons")
    fun reportReasonList(
        @RequestParam(defaultValue = "MEMBER")
        type: ReportTargetType
    ): ResponseEntity<ApiResponse<List<ReportReasonResponse>>>
    {
        val response = reportReasonQueryService.findAllReportReason(type)

        return ResponseEntity.ok(ApiResponse.success(response))

    }


}