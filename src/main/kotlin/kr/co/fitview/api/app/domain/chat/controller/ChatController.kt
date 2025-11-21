package kr.co.fitview.api.app.domain.chat.controller

import jakarta.validation.Valid
import kr.co.fitview.api.app.domain.chat.condition.ChatCondition
import kr.co.fitview.api.app.domain.chat.dto.request.ChatRoomCreateRequest
import kr.co.fitview.api.app.domain.chat.dto.response.ChatRoomCreateResponse
import kr.co.fitview.api.app.domain.chat.dto.response.ChatRoomResponse
import kr.co.fitview.api.app.domain.chat.dto.response.LastChatMessage
import kr.co.fitview.api.app.domain.chat.service.ChatMessageService
import kr.co.fitview.api.app.domain.chat.service.ChatRoomService
import kr.co.fitview.api.app.domain.chat.service.ChatService
import kr.co.fitview.api.app.global.dto.ApiResponse
import kr.co.fitview.api.app.global.dto.SuccessCursorAtPagedResponse
import kr.co.fitview.api.app.global.util.SecurityUtil
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/v1/chats")
class ChatController(
    private val chatService : ChatService,
    private val chatRoomService : ChatRoomService,
    private val chatMessageService : ChatMessageService,
    private val securityUtil : SecurityUtil
) {

    @PostMapping("")
    fun chatRoomAdd(
        @Valid
        @RequestBody
        request : ChatRoomCreateRequest
    ) : ResponseEntity<ApiResponse<ChatRoomCreateResponse>> {
        val response = chatService.saveChatRoom(securityUtil.getMemberId(), request.toServiceRequest())
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @GetMapping("")
    fun chatRoomList(
        @ModelAttribute
        condition : ChatCondition
    ) : ResponseEntity<ApiResponse<SuccessCursorAtPagedResponse<ChatRoomResponse>>> {

        val response = chatRoomService.findChatRooms(securityUtil.getMemberId(), condition)

        return ResponseEntity.ok(ApiResponse.successWithCursorAtPagination(
            slice = response,
            timeExtractor = { it.lastChatMessage.sentAt },
        ))
    }

    @GetMapping("/{chatRoomId}/messages")
    fun chatMessageList(
        @PathVariable
        chatRoomId : Long,

        @ModelAttribute
        condition : ChatCondition
    ) : ResponseEntity<ApiResponse<SuccessCursorAtPagedResponse<LastChatMessage>>> {

        val response = chatMessageService.findChatMessages(securityUtil.getMemberId(), chatRoomId, condition)

        return ResponseEntity.ok(ApiResponse.successWithCursorAtPagination(
            slice = response,
            timeExtractor = { it.sentAt },
        ))
    }


}