package kr.co.fitview.api.app.domain.workout.service

import com.fasterxml.jackson.annotation.JsonIgnore
import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.dto.request.ChatWorkoutRequestMessageServiceRequest
import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.chat.repository.ChatMessageRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatParticipantRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatRoomRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.term.entity.enums.TermName
import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequest
import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import kr.co.fitview.api.app.domain.workout.repository.WorkoutRequestRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class WorkoutRequestServiceTest @Autowired constructor(
    val workoutRequestRepository : WorkoutRequestRepository,
    val workoutRequestService: WorkoutRequestService,
    val memberRepository : MemberRepository,
    val chatRoomRepository : ChatRoomRepository,
    val chatParticipantRepository : ChatParticipantRepository,
    val chatMessageRepository : ChatMessageRepository,
    val time : Time
) : IntegrationTestSupport() {

    @DisplayName("운동 요청을 추가한다.")
    @Test
    fun addWorkoutRequest() {
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

        val chatParticipant1 = ChatParticipant(
            chatRoom,
            me
        )
        val chatParticipant2 = ChatParticipant(
            chatRoom,
            other
        )
        chatParticipantRepository.save(chatParticipant1)
        chatParticipantRepository.save(chatParticipant2)

        val chatMessage = ChatMessage.ofWorkoutRequest(
            member = me,
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage)


        val request = ChatWorkoutRequestMessageServiceRequest(
            type = ChatMessageType.WORKOUT_REQUEST,
            scheduledAt = time.nowLocalDateTime.plusHours(24),
            location = "location"
        )

        // when
        workoutRequestService.addWorkoutRequest(
            chatMessage = chatMessage,
            fromMember = me,
            toMember = other,
            message = request
        )

        // then
        val findWorkoutRequest = workoutRequestRepository.findAll()[0]
        assertThat(findWorkoutRequest)
            .extracting(
                "chatMessage",
                "fromMember",
                "toMember",
                "status",
                "location",
                "scheduledAt",
                "requestedAt")
            .contains(
                chatMessage,
                me,
                other,
                WorkoutRequestStatus.PENDING,
                "location",
                time.nowLocalDateTime.plusHours(24),
                time.nowLocalDateTime
            )
    }

    @DisplayName("만료된 운동 요청을 전부 만료로 표시한다.")
    @Test
    fun modifyAllWorkoutRequestExpire() {
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

        val chatParticipant1 = ChatParticipant(
            chatRoom,
            me
        )
        val chatParticipant2 = ChatParticipant(
            chatRoom,
            other
        )
        chatParticipantRepository.save(chatParticipant1)
        chatParticipantRepository.save(chatParticipant2)

        val chatMessage = ChatMessage.ofWorkoutRequest(
            member = me,
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage)

        val workoutRequest1 = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        val expireWorkoutRequest1 = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime.minusHours(24)
        )
        val expireWorkoutRequest2 = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.minusHours(3),
            requestedAt = time.nowLocalDateTime
        )
        workoutRequestRepository.save(workoutRequest1)
        workoutRequestRepository.save(expireWorkoutRequest1)
        workoutRequestRepository.save(expireWorkoutRequest2)


        // when
        val response = workoutRequestService.modifyAllWorkoutRequestExpire()

        // then
        assertThat(response)
            .extracting("chatRoomId", "workoutRequestId", "status", "fromMemberId", "toMemberId")
            .containsExactlyInAnyOrder(
                tuple(chatRoom.id!!, expireWorkoutRequest1.id!!, WorkoutRequestStatus.EXPIRE, me.id!!, other.id!!),
                tuple(chatRoom.id!!, expireWorkoutRequest2.id!!, WorkoutRequestStatus.EXPIRE, me.id!!, other.id!!),
            )
    }

}