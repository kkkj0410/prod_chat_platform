package kr.co.fitview.api.app

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@SpringBootTest
class AppApplicationTests: IntegrationTestSupport() {

	@Test
	fun contextLoads() {
	}

}
