package kr.co.fitview.api.app.global.stomp.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.repository.ChatMessageRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatNoticeMessageRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatParticipantRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatRoomRepository
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.workout.repository.WorkoutRequestRepository
import kr.co.fitview.api.app.global.scheduler.WorkoutRequestExpireScheduler
import kr.co.fitview.api.app.global.stomp.constant.StompConstant
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.util.MimeTypeUtils

class StompHeaderFactoryTest  @Autowired constructor(
    val stompHeaderFactory: StompHeaderFactory,

) : IntegrationTestSupport() {

    @DisplayName("stomp에 clientRequestId 헤더를 만든다.")
    @Test
    fun createHeader() {
        //given
        val requestId = "test-uuid-1234"

        // when
        val result = stompHeaderFactory.createHeader(requestId)

        // then
        assertThat(result.contentType).isEqualTo(MimeTypeUtils.APPLICATION_JSON)
        assertThat(result.getFirstNativeHeader("clientRequestId"))
            .isEqualTo(requestId)
    }

}