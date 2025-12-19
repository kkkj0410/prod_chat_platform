package kr.co.fitview.api.app.global.redis.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import kr.co.fitview.api.app.global.redis.constant.RedisConstant
import kr.co.fitview.api.app.global.redis.dto.request.RedisEventEnvelope
import kr.co.fitview.api.app.global.redis.enums.StompEventType
import kr.co.fitview.api.app.global.stomp.dto.request.*
import kr.co.fitview.api.app.global.stomp.service.StompPublishService
import org.hibernate.query.sqm.tree.SqmNode.log
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.stereotype.Service

@Service
class RedisStompService(
    private val redisClient : RedisClient,
    private val stompPublishService : StompPublishService,
    private val objectMapper: ObjectMapper
)  : MessageListener {


    // 해당 장소에서 redis 이벤트를 발생
    // redisConfig의 redisMessageListenerContainer은 해당 이벤트를 읽고, messageListenerAdapter를 통해 onMessage 함수를 강제 실행
    fun publishStompEvent(event: Any) {

        val type = StompEventType.from(event::class.java)
        val bodyNode = objectMapper.valueToTree<JsonNode>(event)

        val envelope = RedisEventEnvelope(
            type = type,
            body = bodyNode
        )

        val jsonString = objectMapper.writeValueAsString(envelope)
        redisClient.convertAndSend(RedisConstant.STOMP_TOPIC, jsonString)
    }

    override fun onMessage(message: Message, pattern: ByteArray?) {

        val payloadJson = String(message.body)

        val envelope = objectMapper.readValue(payloadJson, RedisEventEnvelope::class.java)

        val actualEvent = objectMapper.treeToValue(envelope.body, envelope.type.clazz)

        sendStompMessage(actualEvent)
    }

    private fun sendStompMessage(event: Any) {
        when (event) {
            is StompEventTextMessageDepth1 ->
                stompPublishService.sendChatTextMessage(event)

            is StompEventWorkoutRequestMessageDepth1 ->
                stompPublishService.sendChatWorkoutRequestMessage(event)

            is StompEventChatNoticeMessageDepth1 ->
                stompPublishService.sendChatNoticeMessage(event)

            is StompEventUpdateWorkoutRequestMessageDepth1 ->
                stompPublishService.sendUpdateWorkoutRequest(event)

            is StompEventWorkoutPartnerRequestDepth1 ->
                stompPublishService.sendWorkoutPartnerRequest(event)

            is StompEventAcceptWorkoutPartnerDepth1 ->
                stompPublishService.sendAcceptWorkoutPartner(event)

            else -> {
                log.info("Unknown event type received: ${event::class.java.name}")
            }
        }
    }

}