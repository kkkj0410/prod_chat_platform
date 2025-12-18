package kr.co.fitview.api.app.global.redis.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.global.stomp.dto.request.StompEventTextMessageDepth1
import kr.co.fitview.api.app.global.stomp.dto.request.StompEventTextMessageDepth2
import kr.co.fitview.api.app.global.stomp.dto.request.StompEventTextMessageDepth3
import kr.co.fitview.api.app.global.time.Time
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class RedisStompServiceTest @Autowired constructor(
    private val redisStompService: RedisStompService,
    private val time : Time
) : IntegrationTestSupport(){


    @DisplayName("각 서버에게 stomp 이벤트를 전달한다.")
    @Test
    fun publishStompEvent() {
        // given
        val event = StompEventTextMessageDepth1(
            memberId = 123L,
            message = StompEventTextMessageDepth2(
                chatRoomId = 1L,
                isCompleteWorkout = true,
                profileImageUrl = "profile",
                nickname = "nick",
                chatMessage = StompEventTextMessageDepth3(
                    chatMessageId = 2L,
                    content = "content",
                    sentAt = time.nowLocalDateTime,
                    isMe = true
                )
            )
        )

        // when

        // then

    }
}