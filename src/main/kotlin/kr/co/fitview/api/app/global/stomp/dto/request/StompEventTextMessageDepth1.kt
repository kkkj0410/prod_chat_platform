package kr.co.fitview.api.app.global.stomp.dto.request

import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
data class StompEventTextMessageDepth1 (
    val memberId : Long,
    val message : StompEventTextMessageDepth2
)