package kr.co.fitview.api.app.domain.chat.service

import jakarta.persistence.EntityManager
import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.chat.repository.ChatMessageRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatParticipantRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatRoomRepository
import kr.co.fitview.api.app.domain.chat.repository.MessageReadStatusRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequest
import kr.co.fitview.api.app.domain.workout.repository.WorkoutRequestRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.hibernate.proxy.HibernateProxy
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ChatRoomQueryServiceTest @Autowired constructor(
    private val chatRoomQueryService: ChatRoomQueryService,
    private val chatRoomRepository: ChatRoomRepository,
    private val chatParticipantRepository: ChatParticipantRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val workoutRequestRepository: WorkoutRequestRepository,
    private val memberRepository: MemberRepository,
    private val time: Time,
    private val em : EntityManager
) : IntegrationTestSupport(){

    @DisplayName("운동 요청이 있는 채팅방을 조회한다.")
    @Test
    fun findChatRoomFromWorkoutRequestId() {
        //given
        val me = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        val other1 = Member(
            email = "email2",
            password = "password2",
            role = Role.USER,
        )
        memberRepository.save(me)
        memberRepository.save(other1)


        val chatRoom1 = chatRoomRepository.save(ChatRoom(ChatRoomType.PRIVATE))

        val chatParticipant1 = ChatParticipant(
            chatRoom1,
            me
        )
        val chatParticipant2 = ChatParticipant(
            chatRoom1,
            other1
        )

        chatParticipantRepository.save(chatParticipant1)
        chatParticipantRepository.save(chatParticipant2)


        val chatMessage1 = ChatMessage(
            member = me,
            chatRoom = chatRoom1,
            type = ChatMessageType.WORKOUT_REQUEST,
            content = "content",
            sentAt = time.nowLocalDateTime
        )
        val workoutRequest = WorkoutRequest.of(
            chatMessage = chatMessage1,
            fromMember = me,
            toMember = other1,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage1)
        workoutRequestRepository.save(workoutRequest)

        // when
        val findChatRoom = chatRoomQueryService.findChatRoomFrom(workoutRequest.id!!)

        // then
        assertThat(findChatRoom).isEqualTo(chatRoom1)
    }

    @DisplayName("채팅방을 프록시로 전체 조회한다.")
    @Test
    fun findAllChatRoomReferenceFrom() {
        // given
        val chatRoom1 = ChatRoom(ChatRoomType.PRIVATE)
        val chatRoom2 = ChatRoom(ChatRoomType.PRIVATE)
        chatRoomRepository.save(chatRoom1)
        chatRoomRepository.save(chatRoom2)

        val chatRoomIds = listOf(chatRoom1.id!!, chatRoom2.id!!)

        em.flush()
        em.clear()

        // when
        val chatRoomProxy = chatRoomQueryService.findAllChatRoomReferenceFrom(chatRoomIds)

        // then
        assertThat(chatRoomProxy).hasSize(2)
        assertThat(chatRoomProxy[0]).isInstanceOf(HibernateProxy::class.java)
        assertThat(chatRoomProxy[1]).isInstanceOf(HibernateProxy::class.java)

        assertThat(chatRoomProxy[0].id).isNotNull()
        assertThat(chatRoomProxy[1].id).isNotNull()

    }

}