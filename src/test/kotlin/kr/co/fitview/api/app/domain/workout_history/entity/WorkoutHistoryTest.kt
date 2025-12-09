package kr.co.fitview.api.app.domain.workout_history.entity

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.workout.entity.QWorkoutRequest.workoutRequest
import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequest
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class WorkoutHistoryTest @Autowired constructor(
    val memberRepository : MemberRepository,
    val time : Time
) : IntegrationTestSupport(){

    @DisplayName("회원 순서는 회원 id의 오름차순으로 정렬해서 운동 기록에 담는다.")
    @Test
    fun of() {
        // given
        val member1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        val member2 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member1)
        memberRepository.save(member2)

        val chatRoom = ChatRoom(
            type = ChatRoomType.PRIVATE
        )

        val chatMessage = ChatMessage(
            member = member1,
            chatRoom = chatRoom,
            type = ChatMessageType.WORKOUT_REQUEST,
            content = "content",
            sentAt = time.nowLocalDateTime
        )
        val workoutRequest = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = member1,
            toMember = member2,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )

        // when
        val workoutHistory = WorkoutHistory.of(
            chatRoom = chatRoom,
            workoutRequest = workoutRequest,
            memberOne = member2,
            memberTwo = member1,
            completedAt = time.nowLocalDateTime
        )

        // then
        assertThat(workoutHistory)
            .extracting("memberOne", "memberTwo")
            .contains(member1, member2)
    }

    @DisplayName("첫번째 회원의 id 조회")
    @Test
    fun getMemberOneId() {
        // given
        val member1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        val member2 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member1)
        memberRepository.save(member2)

        val chatRoom = ChatRoom(
            type = ChatRoomType.PRIVATE
        )

        val chatMessage = ChatMessage(
            member = member1,
            chatRoom = chatRoom,
            type = ChatMessageType.WORKOUT_REQUEST,
            content = "content",
            sentAt = time.nowLocalDateTime
        )
        val workoutRequest = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = member1,
            toMember = member2,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )

        // when
        val workoutHistory = WorkoutHistory.of(
            chatRoom = chatRoom,
            workoutRequest = workoutRequest,
            memberOne = member1,
            memberTwo = member2,
            completedAt = time.nowLocalDateTime
        )

        // then
        assertThat(workoutHistory.getMemberOneId()).isEqualTo(member1.id!!)
    }

    @DisplayName("2번째 회원의 id 조회")
    @Test
    fun getMemberTwoId() {
        // given
        val member1 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        val member2 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member1)
        memberRepository.save(member2)

        val chatRoom = ChatRoom(
            type = ChatRoomType.PRIVATE
        )

        val chatMessage = ChatMessage(
            member = member1,
            chatRoom = chatRoom,
            type = ChatMessageType.WORKOUT_REQUEST,
            content = "content",
            sentAt = time.nowLocalDateTime
        )

        val workoutRequest = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = member1,
            toMember = member2,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )

        // when
        val workoutHistory = WorkoutHistory.of(
            chatRoom = chatRoom,
            workoutRequest = workoutRequest,
            memberOne = member1,
            memberTwo = member2,
            completedAt = time.nowLocalDateTime
        )

        // then
        assertThat(workoutHistory.getMemberTwoId()).isEqualTo(member2.id!!)
    }
}