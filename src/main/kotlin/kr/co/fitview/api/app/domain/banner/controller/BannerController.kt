package kr.co.fitview.api.app.domain.banner.controller

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.validation.Valid
import kr.co.fitview.api.app.domain.address.service.AddressService
import kr.co.fitview.api.app.domain.member.condition.MemberLocalCondition
import kr.co.fitview.api.app.domain.address.dto.response.AddressResponse
import kr.co.fitview.api.app.domain.banner.dto.response.BannerActiveResponse
import kr.co.fitview.api.app.domain.banner.entity.enums.BannerType
import kr.co.fitview.api.app.domain.banner.service.BannerQueryService
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
import kr.co.fitview.api.app.global.dto.ApiResponse
import kr.co.fitview.api.app.global.dto.SuccessCursorAtPagedResponse
import kr.co.fitview.api.app.global.dto.SuccessPagedResponse
import kr.co.fitview.api.app.global.util.SecurityUtil
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import software.amazon.awssdk.core.internal.waiters.ResponseOrException.response
import kotlin.random.Random


@RestController
@RequestMapping("/api/v1/banners")
class BannerController(
    val bannerQueryService : BannerQueryService
) {


    @GetMapping("/active")
    fun bannerActiveGet(): ResponseEntity<ApiResponse<List<BannerActiveResponse>>> {

        val response = bannerQueryService.findActiveBanners()

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @PostMapping("/{bannerId}/dismiss")
    fun bannerDismiss(
    ): ResponseEntity<ApiResponse<String>> {

        return ResponseEntity.ok(ApiResponse.success("ok"))
    }


}