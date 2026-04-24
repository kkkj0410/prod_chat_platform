package kr.co.fitview.api.app.global.exception

import jakarta.validation.Valid
import kr.co.fitview.api.app.domain.docs.dto.request.DocsLoginRequest
import kr.co.fitview.api.app.global.dto.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*


// 해당 controller는 실제 사용X
// GlobalExceptionHandler의 각 에러 유형을 Spring Rest docs에 표현하기 위해 사용됨
@RestController
@RequestMapping("/api/v1/exception")
class GlobalExceptionController(
    val globalExceptionService : GlobalExceptionService
){

    @GetMapping("")
    fun exception(
    ): ResponseEntity<ApiResponse<String>> {
        globalExceptionService.throwError()

        return ResponseEntity.ok()
            .body(ApiResponse.success("ok"))
    }

}