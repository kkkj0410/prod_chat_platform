package kr.co.fitview.api.app.domain.chat.controller

import kr.co.fitview.api.app.ControllerTestSupport
import kr.co.fitview.api.app.domain.address.dto.response.AddressDetailResponse
import kr.co.fitview.api.app.domain.address.dto.response.AddressResponse
import kr.co.fitview.api.app.domain.address.entity.enums.AddressSiDo
import kr.co.fitview.api.app.domain.chat.dto.request.ChatRoomCreateRequest
import kr.co.fitview.api.app.domain.chat.dto.response.ChatMessageContent
import kr.co.fitview.api.app.domain.chat.dto.response.ChatMessageWorkoutRequest
import kr.co.fitview.api.app.domain.chat.dto.response.ChatRoomCreateResponse
import kr.co.fitview.api.app.domain.chat.dto.response.ChatRoomResponse
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import kr.co.fitview.api.app.domain.workout.dto.response.LastWorkoutRequestMessage
import kr.co.fitview.api.app.domain.workout.dto.response.enums.WorkoutRequestStatusFor
import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import kr.co.fitview.api.app.global.exception.error.request.RequestErrorCode
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

import org.springframework.test.web.servlet.result.MockMvcResultHandlers
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
                    status = WorkoutRequestStatusFor.PENDING,
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
                    status = WorkoutRequestStatusFor.ACCEPT,
                    scheduledAt = LocalDateTime.now().plusDays(1),
                    location = "스타벅스 앞"
                ),
                lastWorkoutRequest = LastWorkoutRequestMessage(
                    status = WorkoutRequestStatusFor.ACCEPT,
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
                lastWorkoutRequest = LastWorkoutRequestMessage(
                    status = null,
                    chatRoomId = 3L
                )
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

            .andExpect(jsonPath("$.data.pagination.size").value(10))
            .andExpect(jsonPath("$.data.pagination.hasNext").value(false))
            .andExpect(jsonPath("$.data.pagination.cursorAt").exists())
    }


}