package kr.co.fitview.api.app.domain.workout.entity

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.QChatMessage.chatMessage
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.chat.repository.ChatMessageRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatParticipantRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatRoomRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import kr.co.fitview.api.app.domain.workout.repository.WorkoutRequestRepository
import kr.co.fitview.api.app.domain.workout.service.WorkoutRequestService
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired

class WorkoutRequestTest @Autowired constructor(
    val memberRepository : MemberRepository,
    val chatRoomRepository : ChatRoomRepository,
    val chatParticipantRepository : ChatParticipantRepository,
    val chatMessageRepository : ChatMessageRepository,
    val time : Time
) : IntegrationTestSupport(){

    @ParameterizedTest(name = "운동 요청의 상태를 변환한다.")
    @CsvSource("EXPIRE, ACCEPT", "REJECT, CANCEL")
    fun updateStatus(status : String) {
        // given
        val me = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        val other = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )

        val chatRoom = ChatRoom(ChatRoomType.PRIVATE)

        val chatMessage = ChatMessage.ofWorkoutRequest(
            member = me,
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime
        )

        val workoutRequest = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )

        // when
        workoutRequest.updateStatus(WorkoutRequestStatus.valueOf(status))

        // then
        assertThat(workoutRequest.status).isEqualTo(WorkoutRequestStatus.valueOf(status))
    }

    @DisplayName("요청 회원의 id를 조회한다.")
    @Test
    fun getFromMemberId() {
        // given
        val me = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        val other = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(me)
        memberRepository.save(other)

        val chatRoom = ChatRoom(ChatRoomType.PRIVATE)

        val chatMessage = ChatMessage.ofWorkoutRequest(
            member = me,
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime
        )

        val workoutRequest = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )

        //then
        assertThat(workoutRequest.getFromMemberId()).isEqualTo(me.id!!)
    }

    @DisplayName("요청 받은 회원의 id를 조회한다.")
    @Test
    fun getToMemberId() {
        // given
        val me = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        val other = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(me)
        memberRepository.save(other)

        val chatRoom = ChatRoom(ChatRoomType.PRIVATE)

        val chatMessage = ChatMessage.ofWorkoutRequest(
            member = me,
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime
        )

        val workoutRequest = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )

        //then
        assertThat(workoutRequest.getToMemberId()).isEqualTo(other.id!!)
    }

    @DisplayName("해당 운동 요청의 채팅방을 조회한다.")
    @Test
    fun getChatRoomId() {
        // given
        val me = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        val other = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(me)
        memberRepository.save(other)

        val chatRoom = chatRoomRepository.save(ChatRoom(ChatRoomType.PRIVATE))

        val chatMessage = ChatMessage.ofWorkoutRequest(
            member = me,
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage)

        val workoutRequest = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )

        //then
        assertThat(workoutRequest.getChatRoomId()).isEqualTo(chatRoom.id!!)

    }
}