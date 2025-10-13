package kr.co.fitview.api.app

import com.fasterxml.jackson.databind.ObjectMapper
import kr.co.fitview.api.app.domain.auth.controller.AuthController
import kr.co.fitview.api.app.domain.auth.service.AuthService
import kr.co.fitview.api.app.domain.auth.service.RefreshTokenService
import kr.co.fitview.api.app.domain.member.controller.MemberController
import kr.co.fitview.api.app.domain.member.service.MemberService
import kr.co.fitview.api.app.domain.oauth2.controller.OAuth2Controller
import kr.co.fitview.api.app.domain.oauth2.service.AppleService
import kr.co.fitview.api.app.domain.oauth2.service.KakaoService
import kr.co.fitview.api.app.global.config.JwtConfig
import kr.co.fitview.api.app.global.config.SecurityConfig
import kr.co.fitview.api.app.global.config.TestSecurityConfig
import kr.co.fitview.api.app.global.cookie.CookieProvider
import kr.co.fitview.api.app.global.exception.GlobalExceptionHandler
import kr.co.fitview.api.app.global.filter.JwtAuthenticationFilter
import kr.co.fitview.api.app.global.filter.SecurityExceptionFilter
import kr.co.fitview.api.app.global.id.IdGenerator
import kr.co.fitview.api.app.global.id.IdGeneratorProvider
import kr.co.fitview.api.app.global.jwt.JwtAuthentication
import kr.co.fitview.api.app.global.jwt.JwtTokenProvider
import kr.co.fitview.api.app.global.security.JwtAuthenticationEntryPoint
import kr.co.fitview.api.app.global.time.Time
import kr.co.fitview.api.app.global.time.TimeProvider
import kr.co.fitview.api.app.global.util.SecurityUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc


@Import(
    TestSecurityConfig::class,
    SecurityUtil::class
)
@WebMvcTest(controllers = [
    AuthController::class,
    MemberController::class,
    OAuth2Controller::class,
    GlobalExceptionHandler::class
],
excludeFilters = [
    ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = [SecurityConfig::class]
    ),
    ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = [JwtAuthenticationFilter::class]
    ),
    ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = [JwtAuthenticationEntryPoint::class]
    )
])
abstract class ControllerTestSupport {

    @Autowired
    protected lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var authService: AuthService

    @MockitoBean
    private lateinit var memberService: MemberService

    @MockitoBean
    private lateinit var refreshTokenService: RefreshTokenService

    @MockitoBean
    private lateinit var appleService: AppleService

    @MockitoBean
    private lateinit var kakaoService: KakaoService


}