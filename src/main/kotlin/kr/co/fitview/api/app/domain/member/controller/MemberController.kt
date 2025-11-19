package kr.co.fitview.api.app.domain.member.controller

import kr.co.fitview.api.app.domain.address.service.AddressService
import kr.co.fitview.api.app.domain.member.condition.MemberLocalCondition
import kr.co.fitview.api.app.domain.address.dto.response.AddressResponse
import kr.co.fitview.api.app.domain.member.dto.request.Age
import kr.co.fitview.api.app.domain.member.dto.response.*
import kr.co.fitview.api.app.domain.member.dto.response.enums.ProfileWorkoutPartnerStatus
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle
import kr.co.fitview.api.app.domain.member.entity.enums.WorkoutTimeName
import kr.co.fitview.api.app.domain.member.service.MemberService
import kr.co.fitview.api.app.global.dto.ApiResponse
import kr.co.fitview.api.app.global.dto.SuccessPagedResponse
import kr.co.fitview.api.app.global.entity.Gender
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
//        val response = MemberMeResponse2(
//            memberId = 1L,
//            profileImageUrl = "https://example.com/profile.png",
//            nickname = "test_user",
//            gender = Gender.MALE,
//            siDo = "서울특별시",
//            siGunGu = "강남구",
//            eupMyeonDong = "역삼동",
//            intro = "헬스 좋아함",
//            height = 175,
//            weight = 70,
//            age = Age.FIFTIES_AND_ABOVE,
//            workoutExperience = MemberWorkoutExperience.UNDER_ONE_YEAR,
//            workoutStyle = MemberWorkoutStyle.CARDIO,
//            workoutGoal = MemberWorkoutGoal.PERFORMANCE_GOAL,
//            workoutTimeNames = listOf(WorkoutTimeName.WEEKEND_DAWN, WorkoutTimeName.WEEKDAY_MORNING),
//            workoutImageUrls = listOf("https://example.com/workout1.png"),
//            score = 42
//        )
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @DeleteMapping("/me")
    fun memberRemove() : ResponseEntity<ApiResponse<*>> {
        memberService.removeMember(securityUtil.getMemberId())
        return ResponseEntity.ok(ApiResponse.success("ok"))
    }

    @GetMapping("/addresses")
    fun memberAddressDetails(
    ) : ResponseEntity<ApiResponse<AddressResponse>> {

        val response = addressService.findAddressFromMemberId(securityUtil.getMemberId())

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @GetMapping("/recommendations")
    fun memberRecommendationList(

    ) : ResponseEntity<ApiResponse<List<MemberRecommendationResponse>>> {


        val response = memberService.findRandomMemberWithinRecommendation(securityUtil.getMemberId())

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

    @GetMapping("/{memberId}")
    fun memberDetails(
        @PathVariable
        memberId : Long
    ): ResponseEntity<ApiResponse<MemberDetailResponse>> {

        val response = MemberDetailResponse(
            memberId = 1L,
            profileImageUrl = "https://example.com/profile.png",
            nickname = "test_user",
            gender = Gender.MALE,
            siDo = "서울특별시",
            siGunGu = "강남구",
            eupMyeonDong = "역삼동",
            intro = "헬스 좋아함",
            height = 175,
            weight = 70,
            age = Age.FIFTIES_AND_ABOVE,
            workoutExperience = MemberWorkoutExperience.UNDER_ONE_YEAR,
            workoutStyle = MemberWorkoutStyle.CARDIO,
            workoutGoal = MemberWorkoutGoal.PERFORMANCE_GOAL,
            workoutTimeNames = listOf(WorkoutTimeName.WEEKEND_DAWN, WorkoutTimeName.WEEKDAY_MORNING),
            workoutImageUrls = listOf(
                "https://example.com/workout1.png",
                "https://example.com/workout2.png"
            ),
            score = 42,
            workoutPartner = WorkoutPartnerInfoResponse(
                status = ProfileWorkoutPartnerStatus.NONE,
                workoutPartnerRequestId = null,
                chatRoomId = null
            )
        )

        return ResponseEntity.ok(ApiResponse.success(response))
    }



}