package kr.co.fitview.api.app.domain.member.controller

import kr.co.fitview.api.app.domain.member.condition.MemberLocalCondition
import kr.co.fitview.api.app.domain.member.dto.response.MemberAddressResponse
import kr.co.fitview.api.app.domain.member.dto.response.MemberLocalResponse
import kr.co.fitview.api.app.domain.member.dto.response.MemberMeResponse
import kr.co.fitview.api.app.domain.member.dto.response.MemberRecommendationResponse
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle
import kr.co.fitview.api.app.domain.member.service.MemberService
import kr.co.fitview.api.app.global.dto.ApiResponse
import kr.co.fitview.api.app.global.dto.Pagination
import kr.co.fitview.api.app.global.dto.SuccessPagedResponse
import kr.co.fitview.api.app.global.util.SecurityUtil
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/v1/members")
class MemberController(
    val memberService : MemberService,
    val securityUtil : SecurityUtil
) {

    @GetMapping("/me")
    fun memberMe() : ResponseEntity<ApiResponse<MemberMeResponse>> {
        val response = memberService.findMemberMe(securityUtil.getMemberId())
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @GetMapping("/{memberId}/addresses")
    fun memberAddressDetails(
        @PathVariable
        memberId : Long,
    ) : ResponseEntity<ApiResponse<MemberAddressResponse>> {
        val response = MemberAddressResponse(
            addressId = 100L,
            fullAddress = "서울특별시 강남구 테헤란로 123"
        )
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @GetMapping("/recommendations")
    fun memberRecommendationList(
    ) : ResponseEntity<ApiResponse<List<MemberRecommendationResponse>>> {
        val response = listOf(
            MemberRecommendationResponse(
                memberId = 100L,
                nickname = "nickname",
                workoutExperience = MemberWorkoutExperience.JUST_STARTED,
                workoutStyle = MemberWorkoutStyle.CARDIO,
                workoutGoal = MemberWorkoutGoal.PERFORMANCE_GOAL
            )
        )
        // 최대 10개 응답
        return ResponseEntity.ok(ApiResponse.success(response))
    }


    @GetMapping("/local")
    fun memberLocalList(
        @ModelAttribute
        condition : MemberLocalCondition

        ) : ResponseEntity<ApiResponse<SuccessPagedResponse<MemberLocalResponse>>> {
        val response = listOf(
            MemberLocalResponse(
                memberId = 100L,
                nickname = "nickname",
                workoutExperience = MemberWorkoutExperience.JUST_STARTED,
                workoutStyle = MemberWorkoutStyle.CARDIO,
                workoutGoal = MemberWorkoutGoal.PERFORMANCE_GOAL
            )
        )

        val slice = Pagination.from(
            createSampleSlice()
        )

        // 최대 10개 응답
        return ResponseEntity.ok(ApiResponse.successWithPagination(response, slice))
    }
    fun createSampleSlice(): Slice<String> {
        val content = listOf("A", "B", "C") // 임의 데이터
        val pageRequest = PageRequest.of(1, 3)
        val hasNext = true  // 다음 페이지 존재 여부
        return SliceImpl(content, pageRequest, hasNext)
    }

}