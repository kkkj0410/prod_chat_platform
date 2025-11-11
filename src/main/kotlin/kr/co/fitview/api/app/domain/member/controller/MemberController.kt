package kr.co.fitview.api.app.domain.member.controller

import kr.co.fitview.api.app.domain.address.service.AddressService
import kr.co.fitview.api.app.domain.member.condition.MemberLocalCondition
import kr.co.fitview.api.app.domain.address.dto.response.AddressResponse
import kr.co.fitview.api.app.domain.member.dto.response.MemberLocalResponse
import kr.co.fitview.api.app.domain.member.dto.response.MemberMeResponse
import kr.co.fitview.api.app.domain.member.dto.response.MemberRecommendationResponse
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle
import kr.co.fitview.api.app.domain.member.service.MemberService
import kr.co.fitview.api.app.global.dto.ApiResponse
import kr.co.fitview.api.app.global.dto.SuccessPagedResponse
import kr.co.fitview.api.app.global.util.SecurityUtil
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/v1/members")
class MemberController(
    val memberService : MemberService,
    val addressService : AddressService,
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
    ) : ResponseEntity<ApiResponse<AddressResponse>> {

        val response = addressService.findAddressFromMemberId(memberId)

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @GetMapping("/recommendations")
    fun memberRecommendationList(

    ) : ResponseEntity<ApiResponse<List<MemberRecommendationResponse>>> {


        val response = memberService.findRandomMemberWithinRecommendation(securityUtil.getMemberId())

//        val response = listOf(
//            MemberRecommendationResponse(
//                memberId = 100L,
//                nickname = "nickname",
//                workoutExperience = MemberWorkoutExperience.JUST_STARTED,
//                workoutStyle = MemberWorkoutStyle.CARDIO,
//                workoutGoal = MemberWorkoutGoal.PERFORMANCE_GOAL,
//                profileImageUrl = "profileImageUrl",
//                workoutImageUrl = "workoutImageUrl"
//            )
//        )
        // 최대 10개 응답
        return ResponseEntity.ok(ApiResponse.success(response))
    }


    @GetMapping("/local/{seed}")
    fun memberLocalList(
        @ModelAttribute
        condition : MemberLocalCondition,

        @PathVariable
        seed : Long

        ) : ResponseEntity<ApiResponse<SuccessPagedResponse<MemberLocalResponse>>> {

        val response = memberService.findRandomMemberWithinLocal(securityUtil.getMemberId(), condition, seed)

        return ResponseEntity.ok(ApiResponse.successWithPagination(response))
    }


}