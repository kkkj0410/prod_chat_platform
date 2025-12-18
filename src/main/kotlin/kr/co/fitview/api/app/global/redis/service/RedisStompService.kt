package kr.co.fitview.api.app.global.redis.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import kr.co.fitview.api.app.global.redis.constant.RedisConstant
import kr.co.fitview.api.app.global.redis.dto.request.RedisEventEnvelope
import kr.co.fitview.api.app.global.redis.enums.StompEventType
import kr.co.fitview.api.app.global.stomp.dto.request.*
import kr.co.fitview.api.app.global.stomp.service.StompPublishService
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.stereotype.Service

@Service
class RedisStompService(
    private val redisClient : RedisClient,
    private val stompPublishService : StompPublishService,
    private val objectMapper: ObjectMapper
)  : MessageListener {


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


//        // 여기서는 이벤트 종류별로 직접 역직렬화
//        when {
//            payloadJson.contains("StompEventTextMessageDepth1") -> {
//                val event = objectMapper.readValue(payloadJson, StompEventTextMessageDepth1::class.java)
//                stompPublishService.sendChatTextMessage(event)
//            }
//            payloadJson.contains("StompEventWorkoutRequestMessageDepth1") -> {
//                val event = objectMapper.readValue(payloadJson, StompEventWorkoutRequestMessageDepth1::class.java)
//                stompPublishService.sendChatWorkoutRequestMessage(event)
//            }
//            payloadJson.contains("StompEventChatNoticeMessageDepth1") -> {
//                val event = objectMapper.readValue(payloadJson, StompEventChatNoticeMessageDepth1::class.java)
//                stompPublishService.sendChatNoticeMessage(event)
//            }
//            payloadJson.contains("StompEventUpdateWorkoutRequestMessageDepth1") -> {
//                val event = objectMapper.readValue(payloadJson, StompEventUpdateWorkoutRequestMessageDepth1::class.java)
//                stompPublishService.sendUpdateWorkoutRequest(event)
//            }
//            payloadJson.contains("StompEventWorkoutPartnerRequestDepth1") -> {
//                val event = objectMapper.readValue(payloadJson, StompEventWorkoutPartnerRequestDepth1::class.java)
//                stompPublishService.sendWorkoutPartnerRequest(event)
//            }
//            payloadJson.contains("StompEventAcceptWorkoutPartnerDepth1") -> {
//                val event = objectMapper.readValue(payloadJson, StompEventAcceptWorkoutPartnerDepth1::class.java)
//                stompPublishService.sendAcceptWorkoutPartner(event)
//            }
//            else -> throw IllegalArgumentException("Unknown event type")
//        }
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
                // 혹시라도 매핑되지 않은 클래스가 들어왔을 경우
                println("Unknown event type received: ${event::class.java.name}")
            }
        }
    }

}