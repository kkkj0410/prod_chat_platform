package kr.co.fitview.api.app.domain.member.controller

import kr.co.fitview.api.app.domain.member.condition.AdminWithdrawMemberCondition
import kr.co.fitview.api.app.domain.member.condition.AdminMemberCondition
import kr.co.fitview.api.app.domain.member.dto.response.AdminMemberResponse
import kr.co.fitview.api.app.domain.member.dto.response.AdminWithdrawMemberResponse
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle
import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.domain.member.service.MemberWithdrawReasonQueryService
import kr.co.fitview.api.app.global.dto.ApiResponse
import kr.co.fitview.api.app.global.dto.SuccessCursorPagedResponse
import kr.co.fitview.api.app.global.entity.Gender
import kr.co.fitview.api.app.global.entity.OAuth2Provider
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.LocalDateTime


@RestController
@RequestMapping("/api/v1/admins/members")
class AdminMemberController(
    private val memberQueryService : MemberQueryService,
    private val memberWithdrawReasonQueryService: MemberWithdrawReasonQueryService
) {

    @GetMapping("")
    fun memberList(
        @ModelAttribute
        condition : AdminMemberCondition,
    ) : ResponseEntity<ApiResponse<SuccessCursorPagedResponse<AdminMemberResponse>>>
    {
        val response = memberQueryService.findAllMemberFrom(condition)

        return ResponseEntity.ok(
            ApiResponse.successWithCursorPagination(
                slice = response,
                idExtractor = { it.memberId },
            )
        )
    }

    @GetMapping("/withdraw")
    fun memberWithdrawList(
        @ModelAttribute
        condition: AdminWithdrawMemberCondition,
    ): ResponseEntity<ApiResponse<SuccessCursorPagedResponse<AdminWithdrawMemberResponse>>> {

        val response = memberWithdrawReasonQueryService.findAllMemberWithdrawReasonFrom(condition)

        return ResponseEntity.ok(
            ApiResponse.successWithCursorPagination(
                slice = response,
                idExtractor = { it.memberId },
            )
        )
    }

}