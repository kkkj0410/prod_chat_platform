package kr.co.fitview.api.app.domain.member.controller

import kr.co.fitview.api.app.domain.member.condition.AdminWithdrawMemberCondition
import kr.co.fitview.api.app.domain.member.condition.AdminMemberCondition
import kr.co.fitview.api.app.domain.member.dto.response.AdminMemberResponse
import kr.co.fitview.api.app.domain.member.dto.response.AdminWithdrawMemberResponse
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle
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
) {

    @GetMapping("")
    fun memberList(
        @ModelAttribute
        condition : AdminMemberCondition,
    ) : ResponseEntity<ApiResponse<SuccessCursorPagedResponse<AdminMemberResponse>>>
    {
        val allMembers = (1..100).map { i ->
            AdminMemberResponse(
                memberId = i.toLong(),
                email = "testuser$i@example.com",
                provider = OAuth2Provider.GOOGLE,
                nickname = "TestUser$i",
                gender = if (i % 2 == 0) Gender.MALE else Gender.FEMALE,
                birthday = LocalDate.of(1990, (i % 12) + 1, (i % 28) + 1),
                height = 160 + i,
                weight = 55 + i,
                workoutExperience = MemberWorkoutExperience.JUST_STARTED,
                workoutStyle = MemberWorkoutStyle.STRENGTH,
                workoutGoal = MemberWorkoutGoal.PERFORMANCE_GOAL,
                hasWorkoutImageUrl = i % 2 == 0,
                fullAddress = "서울 종로구 부암동 $i",
                reviewCount = i.toLong()
            )
        }.sortedByDescending { it.memberId } // 최신순 정렬

        // lastMemberId 적용 + size만큼 가져오기 (memberId < lastMemberId)
        val filteredMembers = allMembers
            .filter { condition.memberId?.let { lastId -> it.memberId < lastId } ?: true }
            .take(condition.size)

        val filteredMembersWithExtra = allMembers
            .filter { condition.memberId?.let { lastId -> it.memberId < lastId } ?: true }
            .take(condition.size + 1)

        val hasNext = filteredMembersWithExtra.size > condition.size

        // Slice 생성
        val slice: Slice<AdminMemberResponse> = SliceImpl(
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