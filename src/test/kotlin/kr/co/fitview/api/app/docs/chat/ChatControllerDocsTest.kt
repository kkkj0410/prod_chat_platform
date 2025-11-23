package kr.co.fitview.api.app.docs.chat

import kr.co.fitview.api.app.docs.RestDocsHeaders
import kr.co.fitview.api.app.docs.RestDocsPagination
import kr.co.fitview.api.app.docs.RestDocsSupport
import kr.co.fitview.api.app.domain.chat.controller.ChatController
import kr.co.fitview.api.app.domain.chat.dto.request.ChatRoomCreateRequest
import kr.co.fitview.api.app.domain.chat.dto.response.*
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import kr.co.fitview.api.app.domain.chat.service.ChatMessageService
import kr.co.fitview.api.app.domain.chat.service.ChatRoomService
import kr.co.fitview.api.app.domain.chat.service.ChatService
import kr.co.fitview.api.app.domain.workout.dto.response.LastWorkoutRequestMessage
import kr.co.fitview.api.app.domain.workout.dto.response.enums.WorkoutRequestStatusForResponse
import kr.co.fitview.api.app.domain.workout.service.WorkoutRequestService
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.util.SecurityUtil
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.data.domain.*
import org.springframework.http.MediaType
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.operation.preprocess.Preprocessors.*
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters

import org.springframework.restdocs.request.RequestDocumentation.queryParameters
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*

import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime
import java.time.ZoneOffset


class ChatControllerDocsTest : RestDocsSupport() {

    private val chatService: ChatService = mock(ChatService::class.java)
    private val chatRoomService: ChatRoomService = mock(ChatRoomService::class.java)
    private val chatMessageService: ChatMessageService = mock(ChatMessageService::class.java)
    private val workoutRequestService: WorkoutRequestService = mock(WorkoutRequestService::class.java)
    private val securityUtil: SecurityUtil = mock(SecurityUtil::class.java)

    override fun initController(): Any {
        return ChatController(chatService, chatRoomService, chatMessageService, securityUtil, workoutRequestService)
    }

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

            .andDo(
                document(
                    "chat-room-add",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),

                    requestHeaders(
                        RestDocsHeaders.authorizationHeader(Role.USER)
                    ),

                    requestFields(
                        fieldWithPath("toMemberId").type(JsonFieldType.NUMBER)
                            .description("채팅방에 초대할 회원 id"),
                    ),

                    responseFields(
                        fieldWithPath("status").type(JsonFieldType.NUMBER)
                            .description("상태"),
                        fieldWithPath("code").type(JsonFieldType.STRING)
                            .description("코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING)
                            .description("에러 메시지"),
                        fieldWithPath("data").type(JsonFieldType.OBJECT)
                            .description("응답 데이터"),
                        fieldWithPath("data.chatRoomId").type(JsonFieldType.NUMBER)
                            .description("두 회원이 참석한 채팅방 id"),
                    )
                )
            )
    }



    @DisplayName("채팅방 목록 조회 API - TEXT 메시지")
    @Test
    fun chatRoomListTextDocs() {
        // given: TEXT 메시지만 포함한 응답 데이터
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
                    status = WorkoutRequestStatusForResponse.PENDING,
                    chatRoomId = 1L
                )
            )
        )

        val slice: Slice<ChatRoomResponse> = SliceImpl(responses, PageRequest.of(0, 10), false)
        given(chatRoomService.findChatRooms(any(), any())).willReturn(slice)

        mockMvc.perform(
            get("/api/v1/chats")
                .header("Authorization", "Bearer jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .param("size", "10")
                .param("lastMessageAt", "1763714029931")
        )
            .andExpect(status().isOk())
            .andDo(
                document(
                    "chat-room-list-text",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),

                    requestHeaders(RestDocsHeaders.authorizationHeader(Role.USER)),

                    queryParameters(
                        parameterWithName("size").optional()
                            .description("(Optional - default 10) 조회 크기"),
                        parameterWithName("lastMessageAt").optional()
                            .description("(Optional) 해당 부분에 값을 넣으면 해당 시간보다 더 옛날 시점의 채팅방이 조회됨"),
                    ),

                    responseFields(
                        fieldWithPath("status").description("HTTP 상태 코드"),
                        fieldWithPath("code").description("응답 코드"),
                        fieldWithPath("message").description("응답 메시지"),
                        *RestDocsPagination.paginationByCursorAt(),
                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                        fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("채팅방 리스트"),
                        subsectionWithPath("data.content[].lastChatMessage").description("마지막 TEXT 메시지"),
                        subsectionWithPath("data.content[].lastWorkoutRequest").description("마지막 운동 요청 정보"),
                        fieldWithPath("data.content[].chatRoomId").description("채팅방 ID"),
                        fieldWithPath("data.content[].profileImageUrl").description("상대 프로필 이미지 URL"),
                        fieldWithPath("data.content[].nickname").description("상대 닉네임"),
                        fieldWithPath("data.content[].isRead").description("읽음 여부")
                    ),

                    responseFields(
                        beneathPath("data.content[].lastChatMessage").withSubsectionId("text-message"),
                        fieldWithPath("chatMessageId").description("메시지 ID"),
                        fieldWithPath("type").description("TEXT"),
                        fieldWithPath("sentAt").description("보낸 시간"),
                        fieldWithPath("isMe").description("내가 보낸 메시지 여부"),
                        fieldWithPath("content").description("텍스트 내용")
                    ),

                    responseFields(
                        beneathPath("data.content[].lastWorkoutRequest").withSubsectionId("workout-request"),
                        fieldWithPath("status").description("운동 요청 상태" + WorkoutRequestStatusForResponse.allDescription()),
                    )
                )
            )
    }

    @DisplayName("채팅방 목록 조회 API - WORKOUT_REQUEST 메시지")
    @Test
    fun chatRoomListWorkoutRequestDocs() {
        // given: WORKOUT_REQUEST 메시지만 포함한 응답 데이터
        val responses = listOf(
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
                    status = WorkoutRequestStatusForResponse.ACCEPT,
                    chatRoomId = 2L
                )
            )
        )

        val slice: Slice<ChatRoomResponse> = SliceImpl(responses, PageRequest.of(0, 10), false)
        given(chatRoomService.findChatRooms(any(), any())).willReturn(slice)

        mockMvc.perform(
            get("/api/v1/chats")
                .header("Authorization", "Bearer jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .param("size", "10")
                .param("lastMessageAt", "1763714029931")
        )
            .andExpect(status().isOk())
            .andDo(
                document(
                    "chat-room-list-workout-request",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),

                    requestHeaders(RestDocsHeaders.authorizationHeader(Role.USER)),

                    queryParameters(
                        parameterWithName("size").optional()
                            .description("(Optional - default 10) 조회 크기"),
                        parameterWithName("lastMessageAt").optional()
                            .description("(Optional) 해당 부분에 값을 넣으면 해당 시간보다 더 옛날 시점의 채팅방이 조회됨"),
                    ),

                    responseFields(
                        fieldWithPath("status").description("HTTP 상태 코드"),
                        fieldWithPath("code").description("응답 코드"),
                        fieldWithPath("message").description("응답 메시지"),
                        *RestDocsPagination.paginationByCursorAt(),
                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                        fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("채팅방 리스트"),
                        subsectionWithPath("data.content[].lastChatMessage").description("마지막 WORKOUT_REQUEST 메시지"),
                        subsectionWithPath("data.content[].lastWorkoutRequest").description("마지막 운동 요청 정보"),
                        fieldWithPath("data.content[].chatRoomId").description("채팅방 ID"),
                        fieldWithPath("data.content[].profileImageUrl").description("상대 프로필 이미지 URL"),
                        fieldWithPath("data.content[].nickname").description("상대 닉네임"),
                        fieldWithPath("data.content[].isRead").description("읽음 여부")
                    ),

                    responseFields(
                        beneathPath("data.content[].lastChatMessage").withSubsectionId("workout-request-message"),
                        fieldWithPath("chatMessageId").description("메시지 ID"),
                        fieldWithPath("type").description("WORKOUT_REQUEST"),
                        fieldWithPath("sentAt").description("보낸 시간"),
                        fieldWithPath("isMe").description("내가 보낸 메시지 여부"),
                        fieldWithPath("workoutRequestId").description("운동 요청 ID"),
                        fieldWithPath("status").description("운동 요청 상태"),
                        fieldWithPath("scheduledAt").description("운동 예정 시간"),
                        fieldWithPath("location").description("운동 장소")
                    ),

                    responseFields(
                        beneathPath("data.content[].lastWorkoutRequest").withSubsectionId("workout-request"),
                        fieldWithPath("status").description("운동 요청 상태" + WorkoutRequestStatusForResponse.allDescription()),
                    )
                )
            )
    }


    @DisplayName("채팅방 메시지 목록 조회 API - TEXT 메시지")
    @Test
    fun chatMessageListText() {
        val chatRoomId = 456L
        val now = LocalDateTime.now()

        val responses: List<LastChatMessage> = listOf(
            ChatMessageContent(
                chatMessageId = 1L,
                sentAt = now.minusMinutes(10),
                isMe = true,
                content = "안녕하세요!"
            )
        )
        val slice: Slice<LastChatMessage> = SliceImpl(responses, PageRequest.of(0, 10), false)
        given(chatMessageService.findChatMessages(any(), any(), any())).willReturn(slice)

        mockMvc.perform(
            get("/api/v1/chats/{chatRoomId}/messages", chatRoomId)
                .header("Authorization", "Bearer jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .param("size", "10")
                .param("lastMessageAt", now.toEpochSecond(ZoneOffset.UTC).toString())
        )
            .andExpect(status().isOk())
            .andDo(
                document(
                    "chat-message-list-text",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),

                    requestHeaders(
                        RestDocsHeaders.authorizationHeader(Role.USER)
                    ),

                    queryParameters(
                        parameterWithName("size").optional().description("(Optional - default 10) 조회 크기"),
                        parameterWithName("lastMessageAt").optional().description("Optional - 해당 시간보다 더 옛날 시점의 메시지 조회")
                    ),

                    responseFields(
                        fieldWithPath("status").description("HTTP 상태 코드"),
                        fieldWithPath("code").description("응답 코드"),
                        fieldWithPath("message").description("응답 메시지"),
                        *RestDocsPagination.paginationByCursorAt(),
                        fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("채팅 메시지 리스트"),
                        fieldWithPath("data.content[].chatMessageId").description("메시지 ID"),
                        fieldWithPath("data.content[].type").description("TEXT"),
                        fieldWithPath("data.content[].sentAt").description("보낸 시간"),
                        fieldWithPath("data.content[].isMe").description("내가 보낸 메시지 여부"),
                        fieldWithPath("data.content[].content").description("메시지 내용")
                    ),
                )
            )
    }

    @DisplayName("채팅방 메시지 목록 조회 API - WORKOUT_REQUEST 메시지")
    @Test
    fun chatMessageListWorkoutRequestDocs() {
        val chatRoomId = 456L
        val now = LocalDateTime.now()

        val responses: List<LastChatMessage> = listOf(
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
        val slice: Slice<LastChatMessage> = SliceImpl(responses, PageRequest.of(0, 10), false)
        given(chatMessageService.findChatMessages(any(), any(), any())).willReturn(slice)

        mockMvc.perform(
            get("/api/v1/chats/{chatRoomId}/messages", chatRoomId)
                .header("Authorization", "Bearer jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .param("size", "10")
                .param("lastMessageAt", now.toEpochSecond(ZoneOffset.UTC).toString())
        )
            .andExpect(status().isOk())
            .andDo(
                document(
                    "chat-message-list-workout-request",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),

                    requestHeaders(
                        RestDocsHeaders.authorizationHeader(Role.USER)
                    ),

                    queryParameters(
                        parameterWithName("size").optional().description("(Optional - default 10) 조회 크기"),
                        parameterWithName("lastMessageAt").optional().description("Optional - 해당 시간보다 더 옛날 시점의 메시지 조회")
                    ),

                    responseFields(
                        fieldWithPath("status").description("HTTP 상태 코드"),
                        fieldWithPath("code").description("응답 코드"),
                        fieldWithPath("message").description("응답 메시지"),
                        *RestDocsPagination.paginationByCursorAt(),
                        fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("채팅 메시지 리스트"),
                        fieldWithPath("data.content[].chatMessageId").description("메시지 ID"),
                        fieldWithPath("data.content[].type").description("WORKOUT_REQUEST"),
                        fieldWithPath("data.content[].sentAt").description("보낸 시간"),
                        fieldWithPath("data.content[].isMe").description("내가 보낸 메시지 여부"),
                        fieldWithPath("data.content[].workoutRequestId").description("운동 요청 ID"),
                        fieldWithPath("data.content[].status").description("운동 요청 상태"),
                        fieldWithPath("data.content[].scheduledAt").description("운동 예정 시간"),
                        fieldWithPath("data.content[].location").description("운동 장소")

                    ),

                )
            )
    }

    @DisplayName("채팅방 마지막 운동 요청 조회 API")
    @Test
    fun workoutRequestLastDocs() {
        val chatRoomId = 123L

        val lastWorkoutRequest = LastWorkoutRequestMessage(
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
            .andDo(
                document(
                    "chat-workout-request-last",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),

                    requestHeaders(
                        RestDocsHeaders.authorizationHeader(Role.USER)
                    ),

                    pathParameters(
                        parameterWithName("chatRoomId").description("조회할 채팅방 ID")
                    ),

                    responseFields(
                        fieldWithPath("status").description("HTTP 상태 코드"),
                        fieldWithPath("code").description("응답 코드"),
                        fieldWithPath("message").description("응답 메시지"),
                        fieldWithPath("data.status").type(JsonFieldType.STRING).optional()
                            .description("마지막 운동 요청 상태 (없으면 null)" + WorkoutRequestStatusForResponse.allDescription()),
                    )
                )
            )
    }
}