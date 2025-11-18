//package kr.co.fitview.api.app.domain.chat
//
//import kr.co.fitview.api.app.StompTestSupport
//import org.junit.jupiter.api.BeforeEach
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.test.web.server.LocalServerPort
//import org.springframework.messaging.converter.MappingJackson2MessageConverter
//import org.springframework.messaging.simp.stomp.StompFrameHandler
//import org.springframework.messaging.simp.stomp.StompHeaders
//import org.springframework.messaging.simp.stomp.StompSession
//import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter
//import org.springframework.web.socket.messaging.WebSocketStompClient
//import org.springframework.web.socket.sockjs.client.SockJsClient
//import org.springframework.web.socket.sockjs.client.WebSocketTransport
//import java.util.concurrent.BlockingQueue
//import java.util.concurrent.CompletableFuture
//import java.util.concurrent.LinkedBlockingDeque
//import java.util.concurrent.TimeUnit
//
//class StompTest @Autowired constructor(
//    private val
//) : StompTestSupport(){
//
//    @LocalServerPort
//    private var port: Int = 0
//
//    private lateinit var messages: BlockingQueue<MessageResponse>
//
//    @Autowired
//    private lateinit var userRepository: UserRepository
//
//    @Autowired
//    private lateinit var roomRepository: RoomRepository
//
//    @BeforeEach
//    fun setUp() {
//        messages = LinkedBlockingDeque()
//        insertUsers()
//        createRooms()
//    }
//
//    private fun insertUsers() {
//        userRepository.save(User(name = "Alice"))
//        userRepository.save(User(name = "Bob"))
//    }
//
//    private fun createRooms() {
//        roomRepository.save(Room(capacity = 2))
//    }
//
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
//
//    private fun createWebSocketStompClient(): WebSocketStompClient {
//        val transport = WebSocketTransport(StandardWebSocketClient())
//        val sockJsClient = SockJsClient(listOf(transport))
//        return WebSocketStompClient(sockJsClient)
//    }
//}
//
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