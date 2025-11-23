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
import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequest
import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
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
}