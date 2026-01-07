package kr.co.fitview.api.app.domain.member.controller

import jakarta.validation.Valid
import kr.co.fitview.api.app.domain.address.service.AddressService
import kr.co.fitview.api.app.domain.member.condition.MemberLocalCondition
import kr.co.fitview.api.app.domain.address.dto.response.AddressResponse
import kr.co.fitview.api.app.domain.member.condition.MemberReviewCondition
import kr.co.fitview.api.app.domain.member.dto.request.MemberUpdateRequest
import kr.co.fitview.api.app.domain.member.dto.request.MemberWithdrawRequest
import kr.co.fitview.api.app.domain.member.dto.response.*
import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.domain.member.service.MemberService
import kr.co.fitview.api.app.domain.member.service.MemberWithdrawReasonQueryService
import kr.co.fitview.api.app.domain.member.service.MemberWithdrawReasonService
import kr.co.fitview.api.app.domain.review.dto.response.ReviewResponse
import kr.co.fitview.api.app.domain.review.dto.response.ReviewTagCountResponse
import kr.co.fitview.api.app.domain.review.service.ReviewQueryService
import kr.co.fitview.api.app.domain.review.service.ReviewTagCountQueryService
import kr.co.fitview.api.app.domain.workout_partner.entity.enums.WorkoutPartnerRequestStatus
import kr.co.fitview.api.app.global.dto.ApiResponse
import kr.co.fitview.api.app.global.dto.SuccessCursorAtPagedResponse
import kr.co.fitview.api.app.global.dto.SuccessPagedResponse
import kr.co.fitview.api.app.global.util.SecurityUtil
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/v1/members")
class MemberController(
    val memberService : MemberService,
    val memberQueryService : MemberQueryService,
    val memberWithdrawReasonService : MemberWithdrawReasonService,
    val memberWithdrawReasonQueryService : MemberWithdrawReasonQueryService,
    val addressService : AddressService,
    val reviewTagCountQueryService: ReviewTagCountQueryService,
    val reviewQueryService: ReviewQueryService,
    val securityUtil : SecurityUtil
) {

    @GetMapping("/me")
    fun memberMe() : ResponseEntity<ApiResponse<MemberProfileResponse>> {
        val response = memberQueryService.findMemberProfile(securityUtil.getMemberId())

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @DeleteMapping("/me")
    fun memberRemove() : ResponseEntity<ApiResponse<*>> {
        memberService.removeMember(securityUtil.getMemberId())
        return ResponseEntity.ok(ApiResponse.success("ok"))
    }

    @PatchMapping("/me")
    fun memberModify(
        @Valid
        @RequestBody
        request : MemberUpdateRequest
    ) : ResponseEntity<ApiResponse<*>> {

        memberService.modifyMember(securityUtil.getMemberId(), request.toServiceRequest())

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
        @RequestParam(name = "size", defaultValue = "10") size: Int
    ) : ResponseEntity<ApiResponse<List<MemberRecommendationResponse>>> {

        val response = memberQueryService.findRandomMemberWithinRecommendation(securityUtil.getMemberId(), size)

        return ResponseEntity.ok(ApiResponse.success(response))
    }


    @GetMapping("/local/{seed}")
    fun memberLocalList(
        @ModelAttribute
        condition : MemberLocalCondition,

        @PathVariable
        seed : Long

        ) : ResponseEntity<ApiResponse<SuccessPagedResponse<MemberLocalResponse>>> {

        val response = memberQueryService.findRandomMemberWithinLocal(securityUtil.getMemberId(), condition, seed)

        return ResponseEntity.ok(ApiResponse.successWithPagination(response))
    }

    @GetMapping("/{memberId}")
    fun memberDetails(
        @PathVariable
        memberId : Long
    ): ResponseEntity<ApiResponse<MemberDetailResponse>> {
        val response = memberQueryService.findMemberDetail(securityUtil.getMemberId(), memberId)


        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @GetMapping("/{memberId}/reviews/tags")
    fun memberReviewTagList(
        @PathVariable
        memberId : Long
    ): ResponseEntity<ApiResponse<List<ReviewTagCountResponse>>> {

        val response = reviewTagCountQueryService.findAllReviewTagCountFrom(memberId)

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @GetMapping("/{memberId}/reviews")
    fun memberReviewList(
        @PathVariable
        memberId : Long,

        @ModelAttribute
        condition : MemberReviewCondition,

        ): ResponseEntity<ApiResponse<SuccessCursorAtPagedResponse<ReviewResponse>>> {

        val response = reviewQueryService.findReviewFromCondition(memberId, condition)

        return ResponseEntity.ok(ApiResponse.successWithCursorAtPagination(
            slice = response,
            timeExtractor = { it.postedAt }
        ))
    }

    @PostMapping("/withdraw")
    fun memberWithdraw(
        @Valid
        @RequestBody
        request : MemberWithdrawRequest
    ): ResponseEntity<ApiResponse<*>> {

        memberWithdrawReasonService.deleteMember(securityUtil.getMemberId(), request.toServiceRequest())

        return ResponseEntity.ok(ApiResponse.success("ok"))
    }

    @GetMapping("/withdraw")
    fun memberWithdrawList(
    ): ResponseEntity<ApiResponse<List<MemberWithdrawResponse>>> {

        val response = memberWithdrawReasonQueryService.findAllMemberWithdrawReason()

        return ResponseEntity.ok(ApiResponse.success(response))
    }


}