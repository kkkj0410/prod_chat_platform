package kr.co.fitview.api.app

import kr.co.fitview.api.app.domain.fcm.service.FcmPublisher
import kr.co.fitview.api.app.global.stomp.service.StompPublisher
import kr.co.fitview.api.app.global.config.TestJwtConfig
import kr.co.fitview.api.app.global.network.NetworkService
import kr.co.fitview.api.app.global.redis.service.RedisClient
import kr.co.fitview.api.app.global.scheduler.config.TestShedLockConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.event.ApplicationEvents
import org.springframework.test.context.event.RecordApplicationEvents
import org.springframework.transaction.annotation.Transactional


@ActiveProfiles("test")
@Import(
    TestJwtConfig::class
)
@Transactional
@RecordApplicationEvents
@SpringBootTest
abstract class IntegrationTestSupport {

    @MockitoBean
    lateinit var networkService: NetworkService

    @MockitoBean
    lateinit var stompPublisher: StompPublisher

    @MockitoBean
    lateinit var fcmPublisher: FcmPublisher

    @MockitoBean
    lateinit var redisClient: RedisClient

    @Autowired
    lateinit var events : ApplicationEvents

}