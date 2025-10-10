package kr.co.fitview.api.app

import kr.co.fitview.api.app.domain.oauth2.service.AppleAuthService
import kr.co.fitview.api.app.global.config.TestJwtConfig
import kr.co.fitview.api.app.global.network.NetworkService
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.transaction.annotation.Transactional


@ActiveProfiles("test")
@Import(TestJwtConfig::class)
@Transactional
@SpringBootTest
abstract class IntegrationTestSupport {

    @MockitoBean
    lateinit var networkService: NetworkService


}