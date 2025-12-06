package kr.co.fitview.api.app.domain.workout_history.entity

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.global.entity.Role
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class WorkoutHistoryTest @Autowired constructor(
    val memberRepository : MemberRepository
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

        // when
        val workoutHistory = WorkoutHistory.of(
            chatRoom = chatRoom,
            memberOne = member2,
            memberTwo = member1
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

        // when
        val workoutHistory = WorkoutHistory.of(
            chatRoom = chatRoom,
            memberOne = member1,
            memberTwo = member2
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

        // when
        val workoutHistory = WorkoutHistory.of(
            chatRoom = chatRoom,
            memberOne = member1,
            memberTwo = member2
        )

        // then
        assertThat(workoutHistory.getMemberTwoId()).isEqualTo(member2.id!!)
    }
}