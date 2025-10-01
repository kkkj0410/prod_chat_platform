package kr.co.fitview.api.app.domain.member.controller

import kr.co.fitview.api.app.global.dto.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/v1/members")
class MemberController {

    @GetMapping("")
    fun test(
    ): ResponseEntity<ApiResponse<*>> {
        val errorResponse = ApiResponse.fail<Void>(
            HttpStatus.UNAUTHORIZED,
            "AUTH_101",
            "Invalid username or password."
        )
        return ResponseEntity.ok(errorResponse)
    }

}