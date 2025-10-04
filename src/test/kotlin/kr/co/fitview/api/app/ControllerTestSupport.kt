package kr.co.fitview.api.app

import com.fasterxml.jackson.databind.ObjectMapper
import kr.co.fitview.api.app.domain.auth.controller.AuthController
import kr.co.fitview.api.app.domain.member.controller.MemberController
import kr.co.fitview.api.app.global.config.TestSecurityConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc


@Import(TestSecurityConfig::class)
@WebMvcTest(controllers = [
    AuthController::class,
//    MemberController::class
])
abstract class ControllerTestSupport {

    @Autowired
    protected lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var objectMapper: ObjectMapper
}