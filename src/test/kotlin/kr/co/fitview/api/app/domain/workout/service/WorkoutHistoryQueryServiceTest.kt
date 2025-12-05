package kr.co.fitview.api.app.domain.workout.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.chat.repository.ChatRoomRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.workout.entity.WorkoutHistory
import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import kr.co.fitview.api.app.domain.workout.repository.WorkoutHistoryRepository
import kr.co.fitview.api.app.global.entity.Role
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class WorkoutHistoryQueryServiceTest @Autowired constructor(
    val workoutHistoryRepository: WorkoutHistoryRepository,
    val memberRepository : MemberRepository,
    val workoutHistoryQueryService : WorkoutHistoryQueryService,
    val chatRoomRepository : ChatRoomRepository
) : IntegrationTestSupport(){


    @DisplayName("운동 이력 조회")
    @Test
    fun findWorkoutHistoryFrom() {
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
        chatRoomRepository.save(chatRoom)

        val workoutHistory = WorkoutHistory.of(
            chatRoom = chatRoom,
            memberOne = member1,
            memberTwo = member2
        )
        workoutHistoryRepository.save(workoutHistory)

        // when
        val findWorkoutHistory = workoutHistoryQueryService.findWorkoutHistoryFrom(workoutHistory.id!!)

        // then
        assertThat(findWorkoutHistory)
            .extracting("memberOne", "memberTwo")
            .contains(member1, member2)
    }

    @DisplayName("각 채팅방의 운동 완료 여부를 조회한다.")
    @Test
    fun findAllWorkoutHistoryFrom() {
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
        val member3 = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member1)
        memberRepository.save(member2)
        memberRepository.save(member3)

        val chatRoom = ChatRoom(
            type = ChatRoomType.PRIVATE
        )
        val chatRoom2 = ChatRoom(
            type = ChatRoomType.PRIVATE
        )
        chatRoomRepository.save(chatRoom)
        chatRoomRepository.save(chatRoom2)

        val workoutHistory = WorkoutHistory.of(
            chatRoom = chatRoom,
            memberOne = member1,
            memberTwo = member2
        )
        workoutHistoryRepository.save(workoutHistory)

        val chatRoomIds = listOf(chatRoom.id!!, chatRoom2.id!!)

        // when
        val findWorkoutHistories = workoutHistoryQueryService.findAllWorkoutHistoryFrom(chatRoomIds)

        // then
        assertThat(findWorkoutHistories)
            .extracting("chatRoomId", "isCompleteWorkoutHistory")
            .containsExactlyInAnyOrder(
                tuple(chatRoom.id!!, true),
                tuple(chatRoom2.id!!, false),
            )
    }
}