package kr.co.fitview.api.app.domain.fcm.controller

import jakarta.validation.Valid
import kr.co.fitview.api.app.domain.fcm.dto.request.FcmPushRequest
import kr.co.fitview.api.app.domain.fcm.dto.request.FcmTokenCreateRequest
import kr.co.fitview.api.app.domain.fcm.dto.response.FcmTokenActiveResponse
import kr.co.fitview.api.app.domain.fcm.service.AdminFcmTokenService
import kr.co.fitview.api.app.domain.fcm.service.FcmTokenService
import kr.co.fitview.api.app.global.dto.ApiResponse
import kr.co.fitview.api.app.global.util.SecurityUtil
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/v1/admins/fcm-tokens")
class AdminFcmController(
    private val adminFcmTokenService : AdminFcmTokenService,
) {

    @PostMapping("/push")
    fun fcmTokenAdd(
        @Valid
        @RequestBody
        request: FcmPushRequest
    ) {
        adminFcmTokenService.push(request)
    }

    @GetMapping("/active")
    fun fcmTokens(
    ) : ResponseEntity<ApiResponse<List<FcmTokenActiveResponse>>> {
        val response = adminFcmTokenService.findActiveFcmTokens()
        return ResponseEntity.ok(ApiResponse.success(response))
    }

}