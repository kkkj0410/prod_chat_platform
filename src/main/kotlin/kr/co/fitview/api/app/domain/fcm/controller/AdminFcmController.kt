package kr.co.fitview.api.app.domain.fcm.controller

import com.google.firebase.messaging.FirebaseMessagingException
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
        @RequestBody request: FcmPushRequest
    ): ResponseEntity<Any> {
        return try {
            adminFcmTokenService.push(request)
            ResponseEntity.ok().build()
        } catch (e: FirebaseMessagingException) {
            ResponseEntity.badRequest().body(
                mapOf(
                    "exception" to e.javaClass.name,
                    "errorCode" to e.errorCode?.name,
                    "message" to e.message,
                    "cause" to e.cause?.toString(),
                    "stackTrace" to e.stackTrace.map { it.toString() }
                )
            )
        }
    }

    @GetMapping("/active")
    fun fcmTokens(
    ) : ResponseEntity<ApiResponse<List<FcmTokenActiveResponse>>> {
        val response = adminFcmTokenService.findActiveFcmTokens()
        return ResponseEntity.ok(ApiResponse.success(response))
    }

}