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
import org.springframework.messaging.simp.stomp.StompFrameHandler
import org.springframework.messaging.simp.stomp.StompHeaders
import org.springframework.messaging.simp.stomp.StompSession
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter
import org.springframework.test.annotation.Commit
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.messaging.WebSocketStompClient
import org.springframework.web.socket.sockjs.client.SockJsClient
import org.springframework.web.socket.sockjs.client.WebSocketTransport
import java.util.concurrent.*
import kotlin.test.Test
import java.lang.reflect.Type


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

    private var messages: BlockingQueue<String> = LinkedBlockingDeque()

    @DisplayName("메시지를 발행(PUB)한다 - FE에서는 정상 동작하지만 테스트에서는 SUB 내용을 받지 못하는 오류 있음. 해결 필요(2025.11.22)")
    @Commit
//    @Test
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
        val destination = "http://localhost:$port/user/v1/queue/chats/messages"
        val destination2 = "/user/v1/queue/chats/messages"
        val userDestination = "/user/${me.id}/v1/queue/chats/messages"
        session.subscribe(userDestination,
            object : StompFrameHandler {
                override fun getPayloadType(headers: StompHeaders): Type = ChatTextMessageRequest::class.java

                override fun handleFrame(headers: StompHeaders, payload: Any?) {
                    // 수신된 메시지를 큐에 저장하여 테스트 동기화
                    messages.add(payload.toString())
                }
            }
        )

        var d1 = "http://localhost:$port/v1/pub/chats/${savedChatRoom.id!!}/messages"
        var d2 = "/v1/pub/chats/${savedChatRoom.id!!}/messages"
        session.send(d2, request)

        val receivedMessage = messages.poll(5, TimeUnit.SECONDS)

        assertThat(receivedMessage).isNotNull()
        println(receivedMessage)
        assertThat(receivedMessage).contains("hello")

        // then
        val findChatMessages = chatMessageRepository.findAll()
        println(findChatMessages)
    }


}
