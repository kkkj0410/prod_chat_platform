package kr.co.fitview.api.app.domain.chat.controller

import jakarta.validation.Valid
import kr.co.fitview.api.app.domain.chat.condition.ChatCondition
import kr.co.fitview.api.app.domain.chat.dto.request.ChatRoomCreateRequest
import kr.co.fitview.api.app.domain.chat.dto.response.*
import kr.co.fitview.api.app.domain.chat.service.ChatMessageService
import kr.co.fitview.api.app.domain.chat.service.ChatRoomService
import kr.co.fitview.api.app.domain.chat.service.ChatService
import kr.co.fitview.api.app.domain.chat.service.MessageReadStatusService
import kr.co.fitview.api.app.domain.member.dto.response.ChatMemberProfileResponse
import kr.co.fitview.api.app.domain.member.dto.response.MemberChatRoomProfile
import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.domain.workout.dto.response.LastWorkoutRequestMessage
import kr.co.fitview.api.app.domain.workout.service.WorkoutRequestService
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
    private val securityUtil : SecurityUtil,
    private val workoutRequestService : WorkoutRequestService,
    private val messageReadStatusService : MessageReadStatusService,
    private val memberQueryService : MemberQueryService
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
    ) : ResponseEntity<ApiResponse<SuccessCursorAtPagedResponseByChatMessage<LastChatMessage, MemberChatRoomProfile>>> {

        val response = chatMessageService.findChatMessagesWithOtherMember(securityUtil.getMemberId(), chatRoomId, condition)

        return ResponseEntity.ok(SuccessCursorAtPagedResponseByChatMessage.from(
            slice = response.chatMessages,
            otherMember = response.otherMember,
            timeExtractor = { it.sentAt }
        ))
    }

    @GetMapping("/{chatRoomId}/members")
    fun chatMember(
        @PathVariable
        chatRoomId : Long
    ) : ResponseEntity<ApiResponse<ChatMemberProfileResponse>>{
        val response = memberQueryService.findChatMemberFromOrElseThrow(securityUtil.getMemberId(), chatRoomId)

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @GetMapping("/{chatRoomId}/workout-requests/last")
    fun workoutRequestLast(
        @PathVariable
        chatRoomId : Long,
    ) : ResponseEntity<ApiResponse<LastWorkoutRequestMessage?>> {

        val response = workoutRequestService.findRecentWorkoutRequestFrom(listOf(chatRoomId))

        val lastWorkoutRequest = response.firstOrNull()

        return ResponseEntity.ok(ApiResponse.success(lastWorkoutRequest))
    }

    @PatchMapping("/{chatRoomId}/read")
    fun chatMessageRead(
        @PathVariable
        chatRoomId : Long,

    ) : ResponseEntity<ApiResponse<*>> {

        messageReadStatusService.modifyMessageReadStatusFrom(securityUtil.getMemberId(), chatRoomId)

        return ResponseEntity.ok(ApiResponse.success("ok"))
    }





}