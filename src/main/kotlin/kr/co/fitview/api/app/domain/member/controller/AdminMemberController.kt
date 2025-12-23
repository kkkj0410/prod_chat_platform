package kr.co.fitview.api.app.domain.member.controller

import kr.co.fitview.api.app.domain.member.condition.AdminWithdrawMemberCondition
import kr.co.fitview.api.app.domain.member.condition.AdminMemberCondition
import kr.co.fitview.api.app.domain.member.dto.response.AdminMemberResponse
import kr.co.fitview.api.app.domain.member.dto.response.AdminWithdrawMemberResponse
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle
import kr.co.fitview.api.app.domain.member.service.MemberQueryService
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
    private val memberQueryService : MemberQueryService
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

        val allMembers = (1..100).map { i ->
            AdminWithdrawMemberResponse(
                memberId = i.toLong(),
                email = "withdraw$i@example.com",
                provider = OAuth2Provider.GOOGLE,
                nickname = "WithdrawUser$i",
                gender = if (i % 2 == 0) Gender.MALE else Gender.FEMALE,
                birthday = LocalDate.of(1990, (i % 12) + 1, (i % 28) + 1),
                workoutExperience = MemberWorkoutExperience.JUST_STARTED,
                deletedAt = LocalDateTime.now().minusDays(i.toLong()),
                memberWithdrawReasonDisplayText = when (i % 4) {
                    0 -> "서비스 이용 빈도 낮음"
                    1 -> "원하는 기능 부족"
                    2 -> "개인 사정"
                    else -> "기타"
                }
            )
        }.sortedByDescending { it.memberId }

        val filteredMembers = allMembers
            .filter { condition.memberId?.let { lastId -> it.memberId < lastId } ?: true }
            .take(condition.size)

        val filteredMembersWithExtra = allMembers
            .filter { condition.memberId?.let { lastId -> it.memberId < lastId } ?: true }
            .take(condition.size + 1)

        val hasNext = filteredMembersWithExtra.size > condition.size


        val slice: Slice<AdminWithdrawMemberResponse> = SliceImpl(
            filteredMembers,
            PageRequest.of(0, condition.size),
            hasNext
        )

        return ResponseEntity.ok(
            ApiResponse.successWithCursorPagination(
                slice = slice,
                idExtractor = { it.memberId },
            )
        )
    }

}