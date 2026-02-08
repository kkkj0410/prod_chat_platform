package kr.co.fitview.api.app.global.redis.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.global.dto.WsMessageType
import kr.co.fitview.api.app.global.dto.WsResponse
import kr.co.fitview.api.app.global.redis.dto.request.RedisEventEnvelope
import kr.co.fitview.api.app.global.redis.enums.StompEventType
import kr.co.fitview.api.app.global.stomp.constant.StompConstant
import kr.co.fitview.api.app.global.stomp.dto.request.StompEventTextMessageDepth1
import kr.co.fitview.api.app.global.stomp.dto.request.StompEventTextMessageDepth2
import kr.co.fitview.api.app.global.stomp.dto.request.StompEventTextMessageDepth3
import kr.co.fitview.api.app.global.stomp.service.StompPublishService
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.then
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.connection.DefaultMessage
import org.springframework.data.redis.connection.Message

class RedisStompServiceTest @Autowired constructor(
    private val redisStompService: RedisStompService,
    private val stompPublishService : StompPublishService,
    private val time : Time,
    private val objectMapper : ObjectMapper
) : IntegrationTestSupport(){


    @DisplayName("stomp 이벤트를 Redis로 전송한다")
    @Test
    fun publishStompEvent() {
        // given
        val event = StompEventTextMessageDepth1(
            memberId = 123L,
            clientRequestId = "UUID",
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
                ),
            )
        )

        // when
        redisStompService.publishStompEvent(event)

        // then
        then(redisClient).should()
            .convertAndSend(
                eq("stomp"), any<String>()
            )
    }

    @DisplayName("stomp 이벤트를 redis로 보낼 시, 채팅 메시지가 전달된다. ")
    @Test
    fun publishStompEventContainsJson() {
        // given
        val event = StompEventTextMessageDepth1(
            memberId = 123L,
            clientRequestId = "UUID",
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
                ),
            )
        )

        val captor = argumentCaptor<String>()

        // when
        redisStompService.publishStompEvent(event)

        // then
        then(redisClient).should()
            .convertAndSend(eq("stomp"), captor.capture())

        val json = captor.firstValue

        assertThat(json).contains("\"type\":\"CHAT_TEXT_MESSAGE\"")
        assertThat(json).contains("\"memberId\":123")
        assertThat(json).contains("\"chatRoomId\":1")
    }

    @DisplayName("채팅 메시지를 회원에게 전달한다.")
    @Test
    fun onMessage() {
        // given
        val event = StompEventTextMessageDepth1(
            memberId = 123L,
            clientRequestId = "UUID",
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
                ),
            )
        )

        val envelope = RedisEventEnvelope(
            type = StompEventType.CHAT_TEXT_MESSAGE,
            body = objectMapper.valueToTree(event)
        )

        val json = objectMapper.writeValueAsString(envelope)

        val message = DefaultMessage(
            "stomp".toByteArray(),
            json.toByteArray(),
        )

        // when
        redisStompService.onMessage(
            message = message,
            pattern = null
        )

        // then
        then(stompPublisher).should().sendToUser(
            memberId = 123L,
            destination = StompConstant.SUB_CHAT_MESSAGE,
            payload = WsResponse(
                type = WsMessageType.TEXT.code,
                payload = event.message
            ),
            clientRequestId = "UUID"
        )
    }

}