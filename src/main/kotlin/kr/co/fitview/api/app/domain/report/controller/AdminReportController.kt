package kr.co.fitview.api.app.domain.report.controller

import kr.co.fitview.api.app.domain.report.condition.AdminReportCondition
import kr.co.fitview.api.app.domain.report.dto.response.AdminReportResponse
import kr.co.fitview.api.app.domain.report.entity.enums.ReportTargetType
import kr.co.fitview.api.app.domain.report.service.ReportQueryService
import kr.co.fitview.api.app.global.dto.ApiResponse
import kr.co.fitview.api.app.global.dto.SuccessCursorPagedResponse
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime


@RestController
@RequestMapping("/api/v1/admins/reports")
class AdminReportController(
    private val reportQueryService : ReportQueryService
) {


    @GetMapping("")
    fun reportList(
        @ModelAttribute
        condition : AdminReportCondition,
    ) : ResponseEntity<ApiResponse<SuccessCursorPagedResponse<AdminReportResponse>>> {

        val response = reportQueryService.findAllReportFrom(condition)

        return ResponseEntity.ok(
            ApiResponse.successWithCursorPagination(
                slice = response,
                idExtractor = { it.reportId }
            )
        )
//
//        val MEMBER_REASONS = listOf(
//            "약속시간 미준수 / 노쇼",
//            "과도한 개인 정보를 요구",
//            "무례한 발언, 성희롱 등 부적절한 언행",
//            "허위 정보를 기재",
//            "원치 않는 불쾌한 행동",
//            "영업을 목적으로 접근",
//            "기타"
//        )
//
//        val CHAT_ROOM_REASONS = listOf(
//            "불쾌한 언행",
//            "협박 또는 위협적인 내용",
//            "약속을 강요하거나 압박",
//            "개인정보를 요구",
//            "부적절한 요청",
//            "사기 또는 금전 요구 의심",
//            "기타"
//        )
//
//        // 가짜 데이터 100개 (ID 큰 것 → 작은 것, 최신 → 과거)
//        val reports = (1L..100L).map { id ->
//            val targetType =
//                if (id % 2L == 0L) ReportTargetType.MEMBER
//                else ReportTargetType.CHAT_ROOM
//
//            val reasonList =
//                if (targetType == ReportTargetType.MEMBER) MEMBER_REASONS
//                else CHAT_ROOM_REASONS
//
//            AdminReportResponse(
//                reportId = id,
//                fromMemberNickname = "FromUser$id",
//                toMemberNickname = "ToUser$id",
//                reportReasonDisplayText = reasonList[(id % reasonList.size).toInt()],
//                reportDescription = if (id % 3L == 0L) "추가 설명 $id" else null,
//                reportedAt = LocalDateTime.now().minusDays(100 - id),
//                reportTargetType = targetType
//            )
//        }.sortedByDescending { it.reportId }
//        val size = condition.size ?: 10
//        val cursor = condition.reportId
//
//        val filtered = if (cursor == null) {
//            reports
//        } else {
//            reports.filter { it.reportId < cursor }
//        }
//
//        val page = filtered.take(size)
//
//        val filteredWithExtra = reports
//            .filter { cursor?.let { c -> it.reportId < c } ?: true }
//            .take(size + 1)
//
//        val hasNext = filteredWithExtra.size > size
//
//        val slice: Slice<AdminReportResponse> =
//            SliceImpl(page, Pageable.unpaged(), hasNext)
//
//        return ResponseEntity.ok(
//            ApiResponse.successWithCursorPagination(
//                slice = slice,
//                idExtractor = { it.reportId }
//            )
//        )
    }




}