package kr.co.fitview.api.app.global.scheduler

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.repository.ChatMessageRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatNoticeMessageRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatParticipantRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatRoomRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.member.service.MemberService
import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.domain.workout.repository.WorkoutRequestRepository
import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartnerRequest
import kr.co.fitview.api.app.domain.workout_partner.entity.enums.WorkoutPartnerRequestContent
import kr.co.fitview.api.app.domain.workout_partner.entity.enums.WorkoutPartnerRequestStatus
import kr.co.fitview.api.app.domain.workout_partner.repository.WorkoutPartnerRepository
import kr.co.fitview.api.app.domain.workout_partner.repository.WorkoutPartnerRequestRepository
import kr.co.fitview.api.app.global.entity.OAuth2Provider
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class WorkoutPartnerRequestExpireSchedulerTest @Autowired constructor(
    val workoutPartnerRequestExpireScheduler: WorkoutPartnerRequestExpireScheduler,
    val workoutRequestRepository: WorkoutRequestRepository,
    val chatMessageRepository: ChatMessageRepository,
    val chatNoticeMessageRepository: ChatNoticeMessageRepository,
    val workoutPartnerRequestRepository : WorkoutPartnerRequestRepository,
    val workoutPartnerRepository : WorkoutPartnerRepository,
    val memberService : MemberService,
    val memberRepository : MemberRepository,
    val chatRoomRepository : ChatRoomRepository,
    val chatParticipantRepository : ChatParticipantRepository,
    val oAuth2Service : OAuth2Service,
    val time: Time

) : IntegrationTestSupport() {

    @DisplayName("24시간 만료된 운동 파트너 요청을 만료 상태로 전체 바꾼다.")
    @Test
    fun modifyAllExpireWorkoutPartnerRequest() {
        // given
        val me = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE,
            providerId = "providerId"
        )
        val otherMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE,
            providerId = "providerId"
        )
        memberRepository.save(me)
        memberRepository.save(otherMember)

        val workoutPartnerRequest1 = WorkoutPartnerRequest.of(
            fromMember = me,
            toMember = otherMember,
            now = time.nowLocalDateTime.minusHours(24),
            content = WorkoutPartnerRequestContent.BURN
        )
        workoutPartnerRequestRepository.save(workoutPartnerRequest1)

        val workoutPartnerRequest2 = WorkoutPartnerRequest.of(
            fromMember = me,
            toMember = otherMember,
            now = time.nowLocalDateTime.minusHours(24),
            content = WorkoutPartnerRequestContent.BURN
        )
        workoutPartnerRequestRepository.save(workoutPartnerRequest2)


        // when
        workoutPartnerRequestExpireScheduler.modifyAllExpireWorkoutPartnerRequest()

        // then
        val findWorkoutPartnerRequests = workoutPartnerRequestRepository.findAll()
        assertThat(findWorkoutPartnerRequests).hasSize(2)
        assertThat(findWorkoutPartnerRequests[0].status).isEqualTo(WorkoutPartnerRequestStatus.EXPIRE)
        assertThat(findWorkoutPartnerRequests[1].status).isEqualTo(WorkoutPartnerRequestStatus.EXPIRE)
    }
}