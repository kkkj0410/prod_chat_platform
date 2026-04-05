package kr.co.fitview.api.app.domain.workout_partner.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.member.service.MemberService
import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartnerRequest
import kr.co.fitview.api.app.domain.workout_partner.entity.enums.WorkoutPartnerRequestContent
import kr.co.fitview.api.app.domain.workout_partner.repository.WorkoutPartnerRepository
import kr.co.fitview.api.app.domain.workout_partner.repository.WorkoutPartnerRequestRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import kr.co.fitview.api.app.global.util.TestDataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class WorkoutPartnerServiceTest @Autowired constructor(
    val workoutPartnerService: WorkoutPartnerService,
    val workoutPartnerRepository : WorkoutPartnerRepository,
    val workoutPartnerRequestRepository : WorkoutPartnerRequestRepository,
    val memberService : MemberService,
    val memberRepository : MemberRepository,
    val time : Time
) : IntegrationTestSupport(){

    @DisplayName("운동 파트너를 추가한다.")
    @Test
    fun addWorkoutPartner() {
        // given
        val fromMember = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )

        val toMember = Member(
            email = "email2",
            password = "password2",
            role = Role.USER,
        )

        val savedFromMember = memberService.addMember(fromMember)
        val savedToMember = memberService.addMember(toMember)

        val workoutPartnerRequest = WorkoutPartnerRequest.of(
            fromMember = savedFromMember,
            toMember = savedToMember,
            now = time.nowLocalDateTime.minusHours(1),
            content = WorkoutPartnerRequestContent.BURN
        )
        workoutPartnerRequestRepository.save(workoutPartnerRequest)

        // when
        workoutPartnerService.addWorkoutPartner(workoutPartnerRequest)

        // then
        val findWorkoutPartners = workoutPartnerRepository.findAll()
        assertThat(findWorkoutPartners).hasSize(1)
        assertThat(findWorkoutPartners[0])
            .extracting(
                "memberOne.id",
                "memberTwo.id"
            )
            .contains(
                fromMember.id!!,
                toMember.id!!
            )
    }
}