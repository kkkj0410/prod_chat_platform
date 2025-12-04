package kr.co.fitview.api.app

import kr.co.fitview.api.app.domain.fcm.service.FcmPublisher
import kr.co.fitview.api.app.domain.notification.service.StompPublisher
import kr.co.fitview.api.app.domain.oauth2.service.AppleAuthService
import kr.co.fitview.api.app.global.config.TestJwtConfig
import kr.co.fitview.api.app.global.network.NetworkService
import kr.co.fitview.api.app.global.redis.service.RedisClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.transaction.annotation.Transactional
import software.amazon.awssdk.services.s3.presigner.S3Presigner


@ActiveProfiles("test")
@Import(
    TestJwtConfig::class,
)
@Transactional
@SpringBootTest
abstract class IntegrationTestSupport {

    @MockitoBean
    lateinit var networkService: NetworkService

    @MockitoBean
    lateinit var stompPublisher: StompPublisher

    @MockitoBean
    lateinit var redisClient: RedisClient

    @MockitoBean
    lateinit var fcmPublisher: FcmPublisher

}