package kr.co.fitview.api.app.domain.chat.controller

import jakarta.validation.Valid
import kr.co.fitview.api.app.domain.chat.dto.request.ChatRoomCreateRequest
import kr.co.fitview.api.app.domain.chat.dto.response.ChatRoomCreateResponse
import kr.co.fitview.api.app.domain.chat.service.ChatService
import kr.co.fitview.api.app.global.dto.ApiResponse
import kr.co.fitview.api.app.global.util.SecurityUtil
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/v1")
class ChatController(
    private val chatService : ChatService,
    private val securityUtil : SecurityUtil
) {

    @PostMapping("/chats")
    fun chatRoomAdd(
        @Valid
        @RequestBody
        request : ChatRoomCreateRequest
    ) : ResponseEntity<ApiResponse<ChatRoomCreateResponse>> {
        val response = chatService.saveChatRoom(securityUtil.getMemberId(), request.toServiceRequest())
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @PostMapping("/chats/asd")
    fun chatRoomList(
        @RequestParam
        request : ChatRoomCreateRequest
    ) : ResponseEntity<ApiResponse<ChatRoomCreateResponse>> {
        val response = chatService.saveChatRoom(securityUtil.getMemberId(), request.toServiceRequest())
        return ResponseEntity.ok(ApiResponse.success(response))
    }

}