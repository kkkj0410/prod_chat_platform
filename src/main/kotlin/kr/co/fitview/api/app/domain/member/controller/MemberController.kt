package kr.co.fitview.api.app.domain.member.controller

import kr.co.fitview.api.app.domain.member.service.MemberService
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/v1/members")
class MemberController(
    val memberService : MemberService
) {

//    @PostMapping
//    fun memberAdd() : ResponseEntity<ApiResponse<*>>{
//
//    }

}