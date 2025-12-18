package kr.co.fitview.api.app.global.redis.dto.request

import com.fasterxml.jackson.databind.JsonNode
import kr.co.fitview.api.app.global.redis.enums.StompEventType

data class RedisEventEnvelope(
    val type: StompEventType,
    val body: JsonNode
)