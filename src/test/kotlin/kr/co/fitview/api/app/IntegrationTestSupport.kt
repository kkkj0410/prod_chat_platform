package kr.co.fitview.api.app

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional


@ActiveProfiles("test")
@Transactional
@SpringBootTest
abstract class IntegrationTestSupport {
}