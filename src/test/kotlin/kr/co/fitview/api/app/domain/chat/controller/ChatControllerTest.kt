package kr.co.fitview.api.app.domain.chat.controller

import kr.co.fitview.api.app.ControllerTestSupport
import kr.co.fitview.api.app.domain.chat.dto.request.ChatRoomCreateRequest
import kr.co.fitview.api.app.domain.chat.dto.response.*
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import kr.co.fitview.api.app.domain.member.dto.response.MemberChatRoomProfile
import kr.co.fitview.api.app.domain.workout.dto.response.LastWorkoutRequestMessage
import kr.co.fitview.api.app.domain.workout.dto.response.enums.WorkoutRequestStatusForResponse
import kr.co.fitview.api.app.global.exception.error.request.RequestErrorCode
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch

import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

class ChatControllerTest : ControllerTestSupport(){

    @DisplayName("채팅방을 생성한다.")
    @Test
    fun chatRoomAdd() {
        given(chatService.saveChatRoom(any(), any()))
            .willReturn(
                ChatRoomCreateResponse(
                    chatRoomId = 123L
                )
            )

        val request = ChatRoomCreateRequest(
            toMemberId = 1L
        )

        // when // then
        mockMvc.perform(
            post("/api/v1/chats")
                .header("Authorization", "Bearer jwt-token")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isOk())

            .andExpect(jsonPath("$.status").exists())
            .andExpect(jsonPath("$.code").value("ok"))
            .andExpect(jsonPath("$.message").value("ok"))

            .andExpect(jsonPath("$.data.chatRoomId").value(123L))
    }

    @DisplayName("채팅방 생성 시, 상대방 지정은 필수다.")
    @Test
    fun chatRoomAddRequiredChatRoomId() {
        given(chatService.saveChatRoom(any(), any()))
            .willReturn(
                ChatRoomCreateResponse(
                    chatRoomId = 123L
                )
            )

        val request = ChatRoomCreateRequest(
            toMemberId = null
        )

        // when // then
        mockMvc.perform(
            post("/api/v1/chats")
                .header("Authorization", "Bearer jwt-token")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(print())
            .andExpect(status().isBadRequest())

            .andExpect(jsonPath("$.code").value(RequestErrorCode.REQ_FIELD_NOT_VALID.code))
            .andExpect(jsonPath("$.status").value("400"))
            .andExpect(jsonPath("$.message").value("toMemberId is required"))
            .andExpect(jsonPath("$.data").isEmpty())
    }

    @DisplayName("채팅방 목록을 조회한다.")
    @Test
    fun chatRoomList() {

        val responses = listOf(
            ChatRoomResponse(
                chatRoomId = 1L,
                profileImageUrl = "https://example.com/profile1.jpg",
                nickname = "철수",
                isRead = false,
                lastChatMessage = ChatMessageContent(
                    chatMessageId = 101L,
                    type = ChatMessageType.TEXT,
                    sentAt = LocalDateTime.now().minusMinutes(10),
                    isMe = false,
                    isRead = false,
                    chatRoomId = 1L,
                    memberId = 12L,
                    content = "오늘 운동할래?"
                ),
                lastWorkoutRequest = LastWorkoutRequestMessage(
                    workoutRequestId = 123L,
                    status = WorkoutRequestStatusForResponse.PENDING,
                    chatRoomId = 1L
                )
            ),

            ChatRoomResponse(
                chatRoomId = 2L,
                profileImageUrl = "https://example.com/profile2.jpg",
                nickname = "영희",
                isRead = true,
                lastChatMessage = ChatMessageWorkoutRequest(
                    chatMessageId = 202L,
                    type = ChatMessageType.WORKOUT_REQUEST,
                    sentAt = LocalDateTime.now().minusHours(1),
                    isMe = true,
                    isRead = true,
                    chatRoomId = 2L,
                    memberId = 20L,
                    workoutRequestId = 9001L,
                    status = WorkoutRequestStatusForResponse.ACCEPT,
                    scheduledAt = LocalDateTime.now().plusDays(1),
                    location = "스타벅스 앞"
                ),
                lastWorkoutRequest = LastWorkoutRequestMessage(
                    workoutRequestId = 123L,
                    status = WorkoutRequestStatusForResponse.ACCEPT,
                    chatRoomId = 2L
                )
            ),

            ChatRoomResponse(
                chatRoomId = 3L,
                profileImageUrl = "https://example.com/profile3.jpg",
                nickname = "민호",
                isRead = false,
                lastChatMessage = ChatMessageContent(
                    chatMessageId = 303L,
                    type = ChatMessageType.TEXT,
                    sentAt = LocalDateTime.now().minusMinutes(30),
                    isMe = true,
                    isRead = false,
                    chatRoomId = 3L,
                    memberId = 30L,
                    content = "ㅇㅋ 내일 보자"
                ),
                lastWorkoutRequest = null
            )
        )

        val pageRequest = PageRequest.of(0, 10)

        val slice: Slice<ChatRoomResponse> =
            SliceImpl(responses, pageRequest, false)

        given(chatRoomService.findChatRooms(any(), any()))
            .willReturn(
                slice
            )

        mockMvc.perform(
            get("/api/v1/chats")
                .header("Authorization", "Bearer jwt-token")
        )
            .andDo(print())
            .andExpect(status().isOk())

            .andExpect(jsonPath("$.status").exists())
            .andExpect(jsonPath("$.code").value("ok"))
            .andExpect(jsonPath("$.message").value("OK"))

            .andExpect(jsonPath("$.data.content").isArray)
            .andExpect(jsonPath("$.data.content.length()").value(3))

            .andExpect(jsonPath("$.data.content[0].chatRoomId").value(1))
            .andExpect(jsonPath("$.data.content[0].nickname").value("철수"))
            .andExpect(jsonPath("$.data.content[0].lastChatMessage.chatMessageId").value(101))

            .andExpect(jsonPath("$.data.content[0].lastWorkoutRequest.workoutRequestId").value(123L))
            .andExpect(jsonPath("$.data.content[0].lastWorkoutRequest.status").value("PENDING"))

            .andExpect(jsonPath("$.data.pagination.size").value(10))
            .andExpect(jsonPath("$.data.pagination.hasNext").value(false))
            .andExpect(jsonPath("$.data.pagination.cursorAt").exists())
    }

    @DisplayName("채팅방 메시지 목록을 조회한다.")
    @Test
    fun chatMessageList() {
        val chatRoomId = 456L
        val now = LocalDateTime.now()

        val otherMember = MemberChatRoomProfile(
            memberId = 123L,
            nickname = "nickname",
            profileImageUrl = "profile"
        )

        val messages: List<LastChatMessage> = listOf(
            ChatMessageContent(
                chatMessageId = 1L,
                sentAt = now.minusMinutes(10),
                isMe = true,
                content = "안녕하세요!"
            ),
            ChatMessageWorkoutRequest(
                chatMessageId = 2L,
                sentAt = now.minusMinutes(5),
                isMe = false,
                workoutRequestId = 100L,
                status = WorkoutRequestStatusForResponse.PENDING,
                scheduledAt = now.plusDays(1),
                location = "헬스장 앞"
            )
        )
        val sliceChatMessages: Slice<LastChatMessage> = SliceImpl(messages, PageRequest.of(0, 10), false)

        val response = ChatRoomMessageResponse(
            otherMember = otherMember,
            chatMessages = sliceChatMessages
        )

        given(chatMessageService.findChatMessagesWithOtherMember(any(), any(), any())).willReturn(response)

        mockMvc.perform(
            get("/api/v1/chats/{chatRoomId}/messages", chatRoomId)
                .header("Authorization", "Bearer jwt-token")
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").exists())
            .andExpect(jsonPath("$.code").value("ok"))
            .andExpect(jsonPath("$.message").value("OK"))

            .andExpect(jsonPath("$.data.otherMember.memberId").value(123L))
            .andExpect(jsonPath("$.data.otherMember.nickname").value("nickname"))
            .andExpect(jsonPath("$.data.otherMember.profileImageUrl").value("profile"))

            .andExpect(jsonPath("$.data.content").isArray)
            .andExpect(jsonPath("$.data.content.length()").value(2))

            .andExpect(jsonPath("$.data.content[0].chatMessageId").value(1))
            .andExpect(jsonPath("$.data.content[0].type").value("TEXT"))
            .andExpect(jsonPath("$.data.content[0].content").value("안녕하세요!"))
            .andExpect(jsonPath("$.data.content[0].isMe").value(true))

            .andExpect(jsonPath("$.data.content[1].chatMessageId").value(2))
            .andExpect(jsonPath("$.data.content[1].type").value("WORKOUT_REQUEST"))
            .andExpect(jsonPath("$.data.content[1].workoutRequestId").value(100))
            .andExpect(jsonPath("$.data.content[1].status").value("PENDING"))
            .andExpect(jsonPath("$.data.content[1].location").value("헬스장 앞"))

            .andExpect(jsonPath("$.data.pagination.size").value(10))
            .andExpect(jsonPath("$.data.pagination.hasNext").value(false))
            .andExpect(jsonPath("$.data.pagination.cursorAt").exists())
    }

    @DisplayName("채팅방 마지막 운동 요청 조회 API")
    @Test
    fun workoutRequestLast() {
        val chatRoomId = 123L

        // given
        val lastWorkoutRequest = LastWorkoutRequestMessage(
            workoutRequestId = 123L,
            status = WorkoutRequestStatusForResponse.PENDING,
            chatRoomId = chatRoomId
        )
        given(workoutRequestService.findRecentWorkoutRequestFrom(listOf(chatRoomId)))
            .willReturn(listOf(lastWorkoutRequest))

        // when & then
        mockMvc.perform(
            get("/api/v1/chats/{chatRoomId}/workout-requests/last", chatRoomId)
                .header("Authorization", "Bearer jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").exists())
            .andExpect(jsonPath("$.code").value("ok"))
            .andExpect(jsonPath("$.message").value("ok"))
            .andExpect(jsonPath("$.data.workoutRequestId").value(123L))
            .andExpect(jsonPath("$.data.status").value("PENDING"))
    }

    @DisplayName("채팅방 마지막 운동 요청 조회 API")
    @Test
    fun chatMessageRead() {
        val chatRoomId = 123L

        given(messageReadStatusService.modifyMessageReadStatusFrom(any(), any()))
            .willAnswer {}

        // when & then
        mockMvc.perform(
            patch("/api/v1/chats/{chatRoomId}/read", chatRoomId)
                .header("Authorization", "Bearer jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").exists())
            .andExpect(jsonPath("$.code").value("ok"))
            .andExpect(jsonPath("$.message").value("ok"))
            .andExpect(jsonPath("$.data").value("ok"))
    }

}