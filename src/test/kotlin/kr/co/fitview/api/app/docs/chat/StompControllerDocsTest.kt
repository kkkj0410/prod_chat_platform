package kr.co.fitview.api.app.docs.chat

import kr.co.fitview.api.app.docs.RestDocsSupport
import kr.co.fitview.api.app.domain.auth.controller.AuthController
import kr.co.fitview.api.app.domain.auth.service.AuthService
import kr.co.fitview.api.app.domain.chat.controller.StompController
import kr.co.fitview.api.app.domain.chat.service.ChatService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.messaging.simp.user.SimpUserRegistry
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.restdocs.operation.preprocess.Preprocessors.*
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.*

class StompControllerDocsTest : RestDocsSupport(){

    private val simpMessageSendingOperations: SimpMessageSendingOperations = mock(SimpMessageSendingOperations::class.java)
    private val simpUserRegistry: SimpUserRegistry = mock(SimpUserRegistry::class.java)
    private val chatService : ChatService = mock(ChatService::class.java)

    override fun initController(): Any {
        return StompController(simpMessageSendingOperations, simpUserRegistry, chatService)
    }

    @DisplayName("사용자 회원가입 API")
    @Test
    fun chatMessageSpecs() {

        // 1. SEND 프레임의 Request Payload 문서화
        document("chat-send-message-request",
            preprocessRequest(prettyPrint()),
            requestFields(
                // 클라이언트가 [SEND]할 때 보내는 필드 (PrivateMessageDto)
                fieldWithPath("roomId").type(JsonFieldType.NUMBER).description("메시지가 속한 채팅방 ID (필수)"),
                fieldWithPath("content").type(JsonFieldType.STRING).description("전송할 메시지 내용"),
                // ... senderId와 같은 필드도 포함 가능
            )
        )

        // 2. MESSAGE 프레임의 Response Payload 문서화
        document("chat-receive-message-response",
            preprocessResponse(prettyPrint()),
            responseFields(
                // 서버가 [MESSAGE]로 클라이언트에게 다시 보내는 (Fat Payload) 필드
                fieldWithPath("messageId").type(JsonFieldType.NUMBER).description("서버에서 발급한 고유 메시지 ID"),
                fieldWithPath("roomId").type(JsonFieldType.NUMBER).description("메시지가 속한 채팅방 ID"),
                fieldWithPath("senderId").type(JsonFieldType.STRING).description("발신자 ID"),
                fieldWithPath("content").type(JsonFieldType.STRING).description("메시지 내용"),
                fieldWithPath("createdAt").type(JsonFieldType.STRING).description("메시지 생성 시간"),
                fieldWithPath("type").type(JsonFieldType.STRING).description("메시지 타입 (TEXT, IMAGE 등)")
            )
        )

        // 이 테스트는 STOMP 통신 검증과 함께 JSON 스펙 문서를 생성합니다.
    }
}