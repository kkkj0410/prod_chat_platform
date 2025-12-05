package kr.co.fitview.api.app.domain.chat.controller

import jakarta.validation.Valid
import kr.co.fitview.api.app.domain.chat.dto.request.ChatMessageRequest
import kr.co.fitview.api.app.domain.chat.dto.request.WorkoutRequestUpdateRequest
import kr.co.fitview.api.app.domain.chat.service.ChatService
import kr.co.fitview.api.app.domain.notification.service.NotificationStompService
import kr.co.fitview.api.app.domain.workout.service.WorkoutRequestService
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.messaging.simp.user.SimpUserRegistry
import org.springframework.web.bind.annotation.*
import java.security.Principal


@RestController
class StompController(
    private val messageTemplate : SimpMessageSendingOperations,
    private val simpUserRegistry: SimpUserRegistry,
    private val chatService : ChatService,
    private val workoutRequestService : WorkoutRequestService,
    private val notificationStompService : NotificationStompService
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

        @Payload
        request : ChatMessageRequest,

    ) {
        println(request)


        //@SendTo 어노테이션을 대신하는 함수
        messageTemplate.convertAndSend("/api/v1/topic/$roomId", request)
    }

    @MessageMapping("/chats/{chatRoomId}/messages")
    fun sendPrivateMessage(
        principal: Principal,

        @DestinationVariable
        chatRoomId : Long,

        @Valid
        @Payload
        message: ChatMessageRequest
    ) {

        val senderId = principal.name
        val response = chatService.sendMessage(principal.name.toLong(), chatRoomId, message)

        notificationStompService.sendChatNoticeMessage(
            memberId = senderId.toLong(),
            response = response
        )
    }

    @MessageMapping("/workout-requests")
    fun workoutRequestModify(
        principal: Principal,

        @Valid
        @Payload
        request: WorkoutRequestUpdateRequest
    ) {

        val senderId = principal.name

        val response = workoutRequestService.modifyWorkoutRequest(senderId.toLong(), request)

        notificationStompService.sendWorkoutRequestUpdate(
            listOf(response)
        )
    }

    fun isMemberConnected(memberId: String): Boolean {

        // 2. getUser() 함수를 사용하여 해당 Principal(memberId)의 존재 여부 확인
        // memberId는 STOMP 연결 시 설정한 Principal.name과 동일합니다.
        return simpUserRegistry.getUser(memberId) != null
    }

}