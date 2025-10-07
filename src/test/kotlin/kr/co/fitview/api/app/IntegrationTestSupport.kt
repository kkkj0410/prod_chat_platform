package kr.co.fitview.api.app

import kr.co.fitview.api.app.global.config.TestJwtConfig
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional


@ActiveProfiles("test")
@Import(TestJwtConfig::class)
@Transactional
@SpringBootTest
abstract class IntegrationTestSupport {
}