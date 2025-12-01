package kr.co.fitview.api.app.domain.fcm.service

import kr.co.fitview.api.app.IntegrationTestSupport
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class FcmServiceTest @Autowired constructor(
    private val fcmService: FcmService
) : IntegrationTestSupport(){


    @DisplayName("")
    @Test
    fun test() {
        // given

        fcmService.send()

        // when

        // then

    }
}