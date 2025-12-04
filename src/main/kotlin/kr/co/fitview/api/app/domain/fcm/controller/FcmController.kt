package kr.co.fitview.api.app.domain.fcm.controller

import jakarta.validation.Valid
import kr.co.fitview.api.app.domain.fcm.dto.request.FcmTokenCreateRequest
import kr.co.fitview.api.app.domain.fcm.service.FcmService
import kr.co.fitview.api.app.global.dto.ApiResponse
import kr.co.fitview.api.app.global.util.SecurityUtil
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/v1/fcm-tokens")
class FcmController(
    private val fcmService : FcmService,
    private val securityUtil : SecurityUtil
) {

    @PostMapping("")
    fun fcmTokenAdd(
        @Valid
        @RequestBody
        request : FcmTokenCreateRequest
    ) : ResponseEntity<ApiResponse<*>> {

        fcmService.saveFcmToken(securityUtil.getMemberId(), request.toServiceRequest())

        return ResponseEntity.ok(ApiResponse.success("ok"))
    }

}