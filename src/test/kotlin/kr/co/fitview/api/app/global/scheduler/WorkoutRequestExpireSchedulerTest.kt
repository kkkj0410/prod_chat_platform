package kr.co.fitview.api.app.global.scheduler

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.chat.repository.ChatMessageRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatParticipantRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatRoomRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
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

class WorkoutRequestExpireSchedulerTest @Autowired constructor(
    val workoutRequestExpireScheduler: WorkoutRequestExpireScheduler,
    val workoutRequestRepository: WorkoutRequestRepository,
    val chatRoomRepository: ChatRoomRepository,
    val memberRepository: MemberRepository,
    val chatParticipantRepository: ChatParticipantRepository,
    val chatMessageRepository: ChatMessageRepository,
    val time: Time

) : IntegrationTestSupport() {


    @DisplayName("운동 요청의 만료 상태를 갱신한다.")
    @Test
    fun modifyAllExpireWorkoutRequest() {
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
        workoutRequestExpireScheduler.modifyAllExpireWorkoutRequest()

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