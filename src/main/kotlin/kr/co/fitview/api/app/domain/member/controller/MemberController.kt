package kr.co.fitview.api.app.domain.member.controller

import kr.co.fitview.api.app.domain.member.dto.response.MemberMeResponse
import kr.co.fitview.api.app.domain.member.service.MemberService
import kr.co.fitview.api.app.global.dto.ApiResponse
import kr.co.fitview.api.app.global.util.SecurityUtil
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

}