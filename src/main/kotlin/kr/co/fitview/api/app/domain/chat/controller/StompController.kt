package kr.co.fitview.api.app.domain.chat.controller

import kr.co.fitview.api.app.domain.chat.dto.request.ChatMessageRequest
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
//@RequestMapping("/api/v1/chats")
class StompController(
    val messageTemplate : SimpMessageSendingOperations
) {



//    // 방법1. MessaegMapping(수신)과 SendTo(topic에 메시지전달)한꺼번에 처리
//    @MessageMapping("/{roomId}") // 클라이언트에서 특정 publish/roomId 형태로 메시지 발행 시, MessageMapping 수신
//    @SendTo("/topic/{roomId}") // 해당 roomId에 메시지를 발행하여 구독중인 클라이언트에게 메시지 전송
//    fun sendMessage(
//        // @DestinationVariable : @MessageMapping 어노테이션으로 정의된 Websocket Controller 내에서만 사용
//        @DestinationVariable
//        roomId : Long,
//
//        message : String
//    ) : String {
//
//        print(message)
//
//        return message
//    }

    //방법2. MessageMapping 어노테이션만 활용
    // 방법1은 어노테이션에 의존적이라 Redis 붙일때 어려움. 따라서 최대한 개발자가 직접 설정하는 방식
    @MessageMapping("/{roomId}") // 클라이언트에서 특정 publish/roomId 형태로 메시지 발행 시, MessageMapping 수신
    fun sendMessage(
        // @DestinationVariable : @MessageMapping 어노테이션으로 정의된 Websocket Controller 내에서만 사용
        @DestinationVariable
        roomId : Long,

        request : ChatMessageRequest
    ) {

        println(request)

        //@SendTo 어노테이션을 대신하는 함수
        messageTemplate.convertAndSend("/topic/$roomId", request)
    }
}