package kr.co.fitview.api.app.domain.chat

import kr.co.fitview.api.app.StompTestSupport
import kr.co.fitview.api.app.domain.chat.dto.request.ChatTextMessageRequest
import kr.co.fitview.api.app.domain.chat.entity.ChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.chat.repository.ChatMessageRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatParticipantRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatRoomRepository
import kr.co.fitview.api.app.domain.chat.service.ChatService
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.jwt.JwtTokenProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.messaging.converter.MappingJackson2MessageConverter
import org.springframework.messaging.simp.stomp.StompHeaders
import org.springframework.messaging.simp.stomp.StompSession
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.messaging.WebSocketStompClient
import org.springframework.web.socket.sockjs.client.SockJsClient
import org.springframework.web.socket.sockjs.client.WebSocketTransport
import java.util.concurrent.*
import kotlin.test.Test


class StompTest @Autowired constructor(
    val memberRepository : MemberRepository,
    val jwtTokenProvider : JwtTokenProvider,
    val chatService : ChatService,
    val chatMessageRepository : ChatMessageRepository,
    val chatRoomRepository : ChatRoomRepository,
    val chatParticipantRepository : ChatParticipantRepository
) : StompTestSupport(){

    @LocalServerPort
    private var port: Int = 0

//    private lateinit var messages: BlockingQueue<MessageResponse>


//    @BeforeEach
//    fun setUp() {
//        messages = LinkedBlockingDeque()
//    }

    @DisplayName("stomp에 연결한다")
    @Test
    fun accessStomp() {
        //given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(member)

        val accessToken = jwtTokenProvider.createAccessToken(member.id!!, member.role!!)

        val stompClient = WebSocketStompClient(
            SockJsClient(listOf(WebSocketTransport(StandardWebSocketClient())))
        )
        stompClient.messageConverter = MappingJackson2MessageConverter()

        val headers = StompHeaders().apply {
            add("Authorization", "Bearer $accessToken")
        }

        val sessionHandler = object : StompSessionHandlerAdapter() {}

        //when
        val session = stompClient.connectAsync(
            "http://localhost:$port/ws/stomp",
            null,
            headers,
            sessionHandler
        ).get(5, TimeUnit.SECONDS)

        //then
        assertThat(session.isConnected).isTrue()
    }

    @DisplayName("stomp에 연결 시, 유효한 JWT 토큰을 헤더에 전송하지 않으면 거부한다.")
    @Test
    fun accessStompInvalidJwtToken() {
        //given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(member)

        val invalidAccessToken = "asd"

        val stompClient = WebSocketStompClient(
            SockJsClient(listOf(WebSocketTransport(StandardWebSocketClient())))
        )
        stompClient.messageConverter = MappingJackson2MessageConverter()

        val headers = StompHeaders().apply {
            add("Authorization", "Bearer $invalidAccessToken")
        }

        val sessionHandler = object : StompSessionHandlerAdapter() {}

        //when & then
        assertThrows<ExecutionException> {
            stompClient.connectAsync(
                "http://localhost:$port/ws/stomp",
                null,
                headers,
                sessionHandler
            ).get(5, TimeUnit.SECONDS)
        }
    }


    private fun connectStomp(member: Member): StompSession {
        val accessToken = jwtTokenProvider.createAccessToken(member.id!!, member.role!!)

        val stompClient = WebSocketStompClient(
            SockJsClient(listOf(WebSocketTransport(StandardWebSocketClient())))
        ).apply {
            messageConverter = MappingJackson2MessageConverter()
        }

        val headers = StompHeaders().apply {
            add("Authorization", "Bearer $accessToken")
        }

        val sessionHandler = object : StompSessionHandlerAdapter() {}

        // 연결 세션 확보
        val session = stompClient.connectAsync(
            "http://localhost:$port/ws/stomp",
            null,
            headers,
            sessionHandler
        ).get(5, TimeUnit.SECONDS)
        return session
    }

    @DisplayName("메시지를 발행(PUB)한다")
    @Test
    fun publishMessage() {
        //given
        val me = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        val other = Member(
            email = "email2",
            password = "password2",
            role = Role.USER,
        )
        val savedMember1 = memberRepository.save(me)
        val savedMember2 = memberRepository.save(other)

        val savedChatRoom = chatRoomRepository.save(ChatRoom(ChatRoomType.PRIVATE))

        val chatParticipant1 = ChatParticipant(
            savedChatRoom,
            savedMember1
        )
        val chatParticipant2 = ChatParticipant(
            savedChatRoom,
            savedMember2
        )
        chatParticipantRepository.save(chatParticipant1)
        chatParticipantRepository.save(chatParticipant2)

        val session = connectStomp(me)

        val request = ChatTextMessageRequest(
            type = ChatMessageType.TEXT,
            content = "hello"
        )

        // when
        session.send("/v1/pub/chats/${savedChatRoom.id}/messages", request)
        Thread.sleep(500)


        // 나머지 진행하기
        // then

    }




    private fun createWebSocketStompClient(): WebSocketStompClient {
        val transport = WebSocketTransport(StandardWebSocketClient())
        val sockJsClient = SockJsClient(listOf(transport))
        return WebSocketStompClient(sockJsClient)
    }
}
