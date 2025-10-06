package kr.co.fitview.api.app.domain.member.controller

import kr.co.fitview.api.app.domain.member.service.MemberService
import kr.co.fitview.api.app.global.dto.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/v1/admin/members")
class AdminMemberController(
    val memberService : MemberService
) {

    @GetMapping("")
    fun test() : ResponseEntity<ApiResponse<*>> {
        return ResponseEntity.ok(ApiResponse.success("it's a admin test")) }

}