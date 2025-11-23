package kr.co.fitview.api.app.domain.workout.repository

import kr.co.fitview.api.app.IntegrationTestSupport
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
import kr.co.fitview.api.app.domain.workout.dto.response.enums.WorkoutRequestStatusForResponse
import kr.co.fitview.api.app.domain.workout.entity.QWorkoutRequest.workoutRequest
import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequest
import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class WorkoutRequestRepositoryTest @Autowired constructor(
    val workoutRequestRepository : WorkoutRequestRepository,
    val chatRoomRepository : ChatRoomRepository,
    val memberRepository : MemberRepository,
    val chatParticipantRepository : ChatParticipantRepository,
    val chatMessageRepository : ChatMessageRepository,
    val time : Time
) : IntegrationTestSupport() {

    @DisplayName("채팅방의 최근 운동 요청을 조회한다.")
    @Test
    fun findRecentWorkoutRequest() {
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

        val message = ChatMessage(
            member = other,
            chatRoom = chatRoom,
            type = ChatMessageType.WORKOUT_REQUEST,
            sentAt = time.nowLocalDateTime
        )

        val scheduledAt = time.nowLocalDateTime.plusHours(24)
        val requestedAt = time.nowLocalDateTime.minusHours(3)
        val workout = WorkoutRequest(
            chatMessage = message,
            fromMember = other,
            toMember = me,
            status = WorkoutRequestStatus.PENDING,
            location = "location",
            scheduledAt = scheduledAt,
            requestedAt = requestedAt
        )
        chatMessageRepository.save(message)
        workoutRequestRepository.save(workout)

        val chatRoomIds = listOf(chatRoom.id!!)

        //when
        val response = workoutRequestRepository.findRecentWorkoutRequest(chatRoomIds)

        // then
        val status = WorkoutRequestStatusForResponse.from(workout.status!!, requestedAt, scheduledAt, time.nowLocalDateTime)
        assertThat(response[0])
            .extracting("status", "chatRoomId")
            .contains(status, chatRoom.id!!)
    }

    @DisplayName("만료된 운동 요청을 전부 조회한다.")
    @Test
    fun findAllExpireWorkoutRequest() {
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

        val chatMessage1 = ChatMessage.ofWorkoutRequest(
            member = me,
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime
        )
        val chatMessage2 = ChatMessage.ofWorkoutRequest(
            member = me,
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime.minusHours(25)
        )
        val chatMessage3 = ChatMessage.ofWorkoutRequest(
            member = me,
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage1)
        chatMessageRepository.save(chatMessage2)
        chatMessageRepository.save(chatMessage3)

        val workoutRequest1 = WorkoutRequest.of(
            chatMessage = chatMessage1,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        val expireWorkoutRequest1 = WorkoutRequest.of(
            chatMessage = chatMessage2,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime.minusHours(25)
        )
        val expireWorkoutRequest2 = WorkoutRequest.of(
            chatMessage = chatMessage3,
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
        val response = workoutRequestRepository.findAllExpireWorkoutRequest()

        // then
        assertThat(response)
            .extracting("chatRoomId", "workoutRequestId", "status", "fromMemberId", "toMemberId")
            .containsExactlyInAnyOrder(
                tuple(chatRoom.id!!, expireWorkoutRequest1.id!!, WorkoutRequestStatus.PENDING, me.id!!, other.id!!),
                tuple(chatRoom.id!!, expireWorkoutRequest2.id!!, WorkoutRequestStatus.PENDING, me.id!!, other.id!!),
            )
    }

    @DisplayName("만료된 운동 요청을 만료 상태로 갱신한다.")
    @Test
    fun updateExpireByIdIn() {
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

        val chatMessage1 = ChatMessage.ofWorkoutRequest(
            member = me,
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime
        )
        val chatMessage2 = ChatMessage.ofWorkoutRequest(
            member = me,
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime.minusHours(25)
        )
        val chatMessage3 = ChatMessage.ofWorkoutRequest(
            member = me,
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage1)
        chatMessageRepository.save(chatMessage2)
        chatMessageRepository.save(chatMessage3)

        val workoutRequest1 = WorkoutRequest.of(
            chatMessage = chatMessage1,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        val expireWorkoutRequest1 = WorkoutRequest.of(
            chatMessage = chatMessage2,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime.minusHours(25)
        )
        val expireWorkoutRequest2 = WorkoutRequest.of(
            chatMessage = chatMessage3,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.minusHours(3),
            requestedAt = time.nowLocalDateTime
        )
        workoutRequestRepository.save(workoutRequest1)
        workoutRequestRepository.save(expireWorkoutRequest1)
        workoutRequestRepository.save(expireWorkoutRequest2)

        val workoutRequestIds = listOf(expireWorkoutRequest1.id!!, expireWorkoutRequest2.id!!)

        // when
        workoutRequestRepository.updateExpireByIdIn(workoutRequestIds)

        // then
        val workoutRequests = workoutRequestRepository.findAll()
        assertThat(workoutRequests)
            .extracting("id", "status")
            .containsExactlyInAnyOrder(
                tuple(workoutRequest1.id!!, WorkoutRequestStatus.PENDING),
                tuple(expireWorkoutRequest1.id!!, WorkoutRequestStatus.EXPIRE),
                tuple(expireWorkoutRequest2.id!!, WorkoutRequestStatus.EXPIRE),
            )


    }
}