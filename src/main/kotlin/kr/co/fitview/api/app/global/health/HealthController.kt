package kr.co.fitview.api.app.global.health

import kr.co.fitview.api.app.domain.member.dto.response.MemberProfileResponse
import kr.co.fitview.api.app.global.dto.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class HealthController {

    @GetMapping("/health3")
    fun healthCheck() : ResponseEntity<*> {
        return ResponseEntity.ok("ok")
    }

}