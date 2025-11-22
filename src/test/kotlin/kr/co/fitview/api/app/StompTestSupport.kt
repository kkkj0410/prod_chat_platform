package kr.co.fitview.api.app

import kr.co.fitview.api.app.domain.oauth2.service.AppleAuthService
import kr.co.fitview.api.app.global.config.TestJwtConfig
import kr.co.fitview.api.app.global.network.NetworkService
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.transaction.annotation.Transactional
import software.amazon.awssdk.services.s3.presigner.S3Presigner


@ActiveProfiles("test")
//@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
//WebEnvironment.RANDOM_PORT -> 다중 스레드 활용으로 인해 트랜젝션 롤백이 안돼서 build 시에 테스트 이후에 데이터가 남아있을 수 있음
// 따라서, test.yml에서는 testdb를 uuid로 계속 바꿔주는 것을 만듦
abstract class StompTestSupport {


}