package kr.co.fitview.api.app.domain.workout_partner.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.entity.ChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.chat.repository.ChatParticipantRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatRoomRepository
import kr.co.fitview.api.app.domain.member.dto.response.enums.ProfileWorkoutPartnerStatus
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartner
import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartnerRequest
import kr.co.fitview.api.app.domain.workout_partner.entity.enums.WorkoutPartnerRequestContent
import kr.co.fitview.api.app.domain.workout_partner.repository.WorkoutPartnerRepository
import kr.co.fitview.api.app.domain.workout_partner.repository.WorkoutPartnerRequestRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class WorkoutPartnerQueryServiceTest @Autowired constructor(
    val workoutPartnerQueryService : WorkoutPartnerQueryService,
    val workoutPartnerRequestRepository : WorkoutPartnerRequestRepository,
    val workoutPartnerRepository : WorkoutPartnerRepository,
    val memberRepository : MemberRepository,
    val chatRoomRepository : ChatRoomRepository,
    val chatParticipantRepository : ChatParticipantRepository,
    val time : Time
) : IntegrationTestSupport(){

    @DisplayName("운동 파트너 요청 확인 시, 파트너 관계이면 파트너 관계를 나타낸다.")
    @Test
    fun findWorkoutPartnerStatusPartner() {
        // given
        val me = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val otherMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(me)
        memberRepository.save(otherMember)

        val workoutPartner = WorkoutPartner.of(
            memberOne = me,
            memberTwo = otherMember
        )
        workoutPartnerRepository.save(workoutPartner)

        // when
        val response = workoutPartnerQueryService.findWorkoutPartnerStatus(me.id!!, otherMember.id!!)

        // then
        assertThat(response)
            .extracting("status", "workoutPartnerRequestId", "chatRoomId")
            .contains(ProfileWorkoutPartnerStatus.PARTNER, null, null)
    }

    @DisplayName("운동 파트너 요청 확인 시, 이미 둘 만의 채팅방이 있으면 채팅방도 알려준다.")
    @Test
    fun findWorkoutPartnerStatusPartnerExistsChatRoom() {
        // given
        val me = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val otherMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(me)
        memberRepository.save(otherMember)

        val workoutPartner = WorkoutPartner.of(
            memberOne = me,
            memberTwo = otherMember
        )
        workoutPartnerRepository.save(workoutPartner)

        val savedChatRoom = chatRoomRepository.save(ChatRoom(ChatRoomType.PRIVATE))

        val chatParticipant1 = ChatParticipant(
            savedChatRoom,
            me
        )
        val chatParticipant2 = ChatParticipant(
            savedChatRoom,
            otherMember
        )
        chatParticipantRepository.save(chatParticipant1)
        chatParticipantRepository.save(chatParticipant2)

        // when
        val response = workoutPartnerQueryService.findWorkoutPartnerStatus(me.id!!, otherMember.id!!)

        // then
        assertThat(response)
            .extracting("status", "workoutPartnerRequestId", "chatRoomId")
            .contains(ProfileWorkoutPartnerStatus.PARTNER, null, savedChatRoom.id!!)
    }

    @DisplayName("운동 파트너 요청 확인 시, 상대가 본인에게 보냈으면 받은 상태를 나타낸다.")
    @Test
    fun findWorkoutPartnerStatusReceive() {
        // given
        val me = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val otherMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(me)
        memberRepository.save(otherMember)

        val workoutPartnerRequest = WorkoutPartnerRequest.of(
            fromMember = otherMember,
            toMember = me,
            now = time.nowLocalDateTime,
            content = WorkoutPartnerRequestContent.BURN
        )
        workoutPartnerRequestRepository.save(workoutPartnerRequest)

        // when
        val response = workoutPartnerQueryService.findWorkoutPartnerStatus(me.id!!, otherMember.id!!)

        // then
        assertThat(response)
            .extracting("status", "workoutPartnerRequestId", "chatRoomId")
            .contains(ProfileWorkoutPartnerStatus.RECEIVE, workoutPartnerRequest.id!!, null)
    }

    @DisplayName("운동 파트너 요청 확인 시, 본인이 상대에게 보냈으면 보낸 상태를 나타낸다.")
    @Test
    fun findWorkoutPartnerStatusSend() {
        // given
        val me = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val otherMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(me)
        memberRepository.save(otherMember)

        val workoutPartnerRequest = WorkoutPartnerRequest.of(
            fromMember = me,
            toMember = otherMember,
            now = time.nowLocalDateTime,
            content = WorkoutPartnerRequestContent.BURN
        )
        workoutPartnerRequestRepository.save(workoutPartnerRequest)

        // when
        val response = workoutPartnerQueryService.findWorkoutPartnerStatus(me.id!!, otherMember.id!!)

        // then
        assertThat(response)
            .extracting("status", "workoutPartnerRequestId", "chatRoomId")
            .contains(ProfileWorkoutPartnerStatus.SEND, workoutPartnerRequest.id!!, null)
    }

    @DisplayName("운동 파트너 요청 확인 시, 본인-상대가 둘 다 보낸 상태라면 받은 상태를 나타낸다.")
    @Test
    fun findWorkoutPartnerStatusReceiveAndSend() {
        // given
        val me = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val otherMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(me)
        memberRepository.save(otherMember)

        val workoutPartnerRequest1 = WorkoutPartnerRequest.of(
            fromMember = me,
            toMember = otherMember,
            now = time.nowLocalDateTime,
            content = WorkoutPartnerRequestContent.BURN
        )
        val workoutPartnerRequest2 = WorkoutPartnerRequest.of(
            fromMember = otherMember,
            toMember = me,
            now = time.nowLocalDateTime,
            content = WorkoutPartnerRequestContent.BURN
        )
        workoutPartnerRequestRepository.save(workoutPartnerRequest1)
        workoutPartnerRequestRepository.save(workoutPartnerRequest2)

        // when
        val response = workoutPartnerQueryService.findWorkoutPartnerStatus(me.id!!, otherMember.id!!)

        // then
        assertThat(response)
            .extracting("status", "workoutPartnerRequestId", "chatRoomId")
            .contains(ProfileWorkoutPartnerStatus.RECEIVE, workoutPartnerRequest2.id!!, null)
    }

    @DisplayName("운동 파트너 요청 확인 시, 보내고/받고 한 요청이 없으면 아무 요청이 없는 상태를 나타낸다.")
    @Test
    fun findWorkoutPartnerStatusNoReceiveAndSend() {
        // given
        val me = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val otherMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(me)
        memberRepository.save(otherMember)


        // when
        val response = workoutPartnerQueryService.findWorkoutPartnerStatus(me.id!!, otherMember.id!!)

        // then
        assertThat(response)
            .extracting("status", "workoutPartnerRequestId", "chatRoomId")
            .contains(ProfileWorkoutPartnerStatus.NONE, null, null)
    }

    @DisplayName("운동 파트너 요청 확인 시, 받은 요청이 있더라도 24시간이 지났으면 상대방은 알 수 없다.")
    @Test
    fun findWorkoutPartnerStatusExpireReceive() {
        // given
        val me = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val otherMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(me)
        memberRepository.save(otherMember)

        val workoutPartnerRequest = WorkoutPartnerRequest.of(
            fromMember = otherMember,
            toMember = me,
            now = time.nowLocalDateTime.minusHours(24),
            content = WorkoutPartnerRequestContent.BURN
        )
        workoutPartnerRequestRepository.save(workoutPartnerRequest)

        // when
        val response = workoutPartnerQueryService.findWorkoutPartnerStatus(me.id!!, otherMember.id!!)

        // then
        assertThat(response)
            .extracting("status", "workoutPartnerRequestId", "chatRoomId")
            .contains(ProfileWorkoutPartnerStatus.NONE, null, null)
    }

    @DisplayName("운동 파트너 요청 확인 시, 보낸 요청이 있더라도 24시간이 지났으면 본인은 알 수 없다.")
    @Test
    fun findWorkoutPartnerStatusExpireSend() {
        // given
        val me = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val otherMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(me)
        memberRepository.save(otherMember)

        val workoutPartnerRequest = WorkoutPartnerRequest.of(
            fromMember = me,
            toMember = otherMember,
            now = time.nowLocalDateTime.minusHours(24),
            content = WorkoutPartnerRequestContent.BURN
        )
        workoutPartnerRequestRepository.save(workoutPartnerRequest)

        // when
        val response = workoutPartnerQueryService.findWorkoutPartnerStatus(me.id!!, otherMember.id!!)

        // then
        assertThat(response)
            .extracting("status", "workoutPartnerRequestId", "chatRoomId")
            .contains(ProfileWorkoutPartnerStatus.NONE, null, null)
    }

    @DisplayName("두 회원이 운동 파트너 관계인지 확인한다.")
    @Test
    fun isWorkoutPartnerFrom() {
        // given
        val me = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val otherMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(me)
        memberRepository.save(otherMember)

        val workoutPartner = WorkoutPartner.of(
            memberOne = me,
            memberTwo = otherMember
        )
        workoutPartnerRepository.save(workoutPartner)

        // when
        val response = workoutPartnerQueryService.isWorkoutPartnerFrom(me.id!!, otherMember.id!!)

        // then
        assertThat(response).isTrue()
    }

    @DisplayName("두 회원이 운동 파트너 관계가 아니면 운동 파트너 관계가 아니다.")
    @Test
    fun isWorkoutPartnerFromNotPartner() {
        // given
        val me = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val otherMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(me)
        memberRepository.save(otherMember)

        // when
        val response = workoutPartnerQueryService.isWorkoutPartnerFrom(me.id!!, otherMember.id!!)

        // then
        assertThat(response).isFalse()
    }

}