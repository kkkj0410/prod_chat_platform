package kr.co.fitview.api.app.domain.chat

import kr.co.fitview.api.app.StompTestSupport
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
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.messaging.WebSocketStompClient
import org.springframework.web.socket.sockjs.client.SockJsClient
import org.springframework.web.socket.sockjs.client.WebSocketTransport
import java.util.concurrent.*
import kotlin.test.Test


class StompTest @Autowired constructor(
    val memberRepository : MemberRepository,
    val jwtTokenProvider : JwtTokenProvider
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


//    @Test
//    fun `유저가 메시지를 보내면 방에 브로드캐스트된다`() {
//        val room = roomRepository.findAll().first()
//        val user = userRepository.findAll().first()
//        val expected = MessageResponse(user.id!!, "안녕하세요!")
//
//        // 1. STOMP WebSocket Client 생성
//        val stompClient = createWebSocketStompClient()
//        stompClient.messageConverter = MappingJackson2MessageConverter()
//
//        // 2. 비동기 연결
//        val stompSessionFuture = CompletableFuture<StompSession>()
//        stompClient.connect(
//            "ws://localhost:$port/ws-connection",
//            object : StompSessionHandlerAdapter() {
//                override fun afterConnected(session: StompSession, headers: StompHeaders) {
//                    stompSessionFuture.complete(session)
//                }
//
//                override fun handleTransportError(session: StompSession, exception: Throwable) {
//                    stompSessionFuture.completeExceptionally(exception)
//                }
//            }
//        )
//
//        val stompSession = stompSessionFuture.get(5, TimeUnit.SECONDS)
//
//        // 3. 메시지 구독
//        stompSession.subscribe("/sub/rooms/${room.id}/chat",
//            StompFrameHandlerImpl(MessageResponse::class.java, messages)
//        )
//
//        // 4. 메시지 발송
//        stompSession.send("/pub/rooms/${room.id}/chat", MessageRequest(user.id!!, "안녕하세요!"))
//
//        // 5. 수신 검증
//        val response = messages.poll(5, TimeUnit.SECONDS)
//        assertThat(response).usingRecursiveComparison().isEqualTo(expected)
//    }

    private fun createWebSocketStompClient(): WebSocketStompClient {
        val transport = WebSocketTransport(StandardWebSocketClient())
        val sockJsClient = SockJsClient(listOf(transport))
        return WebSocketStompClient(sockJsClient)
    }
}

//// STOMP Frame Handler 구현
//class StompFrameHandlerImpl<T>(
//    private val payloadType: Class<T>,
//    private val messages: BlockingQueue<T>
//) : StompFrameHandler {
//    override fun getPayloadType(headers: StompHeaders): Type = payloadType
//    override fun handleFrame(headers: StompHeaders, payload: Any?) {
//        messages.offer(payload as T)
//    }
//}
//
//// 예시 DTO
//data class MessageRequest(val userId: Long, val content: String)
//data class MessageResponse(val userId: Long, val content: String)