package kr.co.fitview.api.app.domain.fcm.controller

import jakarta.validation.Valid
import kr.co.fitview.api.app.domain.chat.dto.request.ChatRoomCreateRequest
import kr.co.fitview.api.app.domain.chat.dto.response.ChatRoomCreateResponse
import kr.co.fitview.api.app.domain.fcm.dto.request.FcmTokenCreateRequest
import kr.co.fitview.api.app.domain.fcm.entity.enums.FcmTokenPlatform
import kr.co.fitview.api.app.domain.fcm.service.FcmService
import kr.co.fitview.api.app.global.dto.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/v1/fcm-tokens")
class FcmController(
    private val fcmService : FcmService
) {

    @PostMapping("")
    fun fcmTokenAdd(
        @Valid
        @RequestBody
        request : FcmTokenCreateRequest
    ) : ResponseEntity<ApiResponse<*>> {
        val request = FcmTokenCreateRequest(
            deviceId = "dfjaiofjoepfjapiefjoiaejfapefj",
            token = "fcmToken",
            platform = FcmTokenPlatform.ANDROID
        )


        return ResponseEntity.ok(ApiResponse.success("ok"))
    }

}