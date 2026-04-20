package kr.co.fitview.api.app.domain.docs.controller

import jakarta.validation.Valid
import kr.co.fitview.api.app.domain.docs.dto.request.DocsLoginRequest
import kr.co.fitview.api.app.domain.docs.service.DocsService
import kr.co.fitview.api.app.global.dto.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


// 해당 API는 API 문서 제공용 로그인 페이지
// => 임시용이기 때문에 테스트X
@RestController
@RequestMapping("/api/v1/docs")
class DocsController(
    val docsService : DocsService
) {


    @PostMapping("/login")
    fun docsLogin(
        @Valid
        @RequestBody
        request: DocsLoginRequest

    ): ResponseEntity<ApiResponse<String>> {
        val response = docsService.login(request.toServiceRequest())

        return ResponseEntity.ok()
            .headers(response)
            .body(ApiResponse.success("ok"))
    }

}