package kr.co.fitview.api.app.domain.workout_partner.repository

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.service.MemberService
import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartner
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class WorkoutPartnerRepositoryTest @Autowired constructor(
    val workoutPartnerRepository : WorkoutPartnerRepository,
    val memberService : MemberService,
    val time : Time
) : IntegrationTestSupport(){


    @DisplayName("회원A-B간의 최신 핏버디 요청을 조회한다.")
    @Test
    fun findTop1ByFromMemberIdAndToMemberIdAndDeletedAtIsNullOrderByRequestedAtDesc(){
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

        val workoutPartner1 = WorkoutPartner(
            savedFromMember,
            savedToMember,
            time.nowLocalDateTime.minusHours(1)
        )
        val workoutPartner2 = WorkoutPartner(
            savedFromMember,
            savedToMember,
            time.nowLocalDateTime
        )
        workoutPartnerRepository.save(workoutPartner1)
        workoutPartnerRepository.save(workoutPartner2)

        // when
        val findWorkPartner = workoutPartnerRepository.findTop1ByFromMemberIdAndToMemberIdAndDeletedAtIsNullOrderByRequestedAtDesc(
            fromMemberId = fromMember.id!!,
            toMemberId = toMember.id!!,
        )

        // then
        assertThat(findWorkPartner)
            .extracting("fromMember", "toMember", "requestedAt")
            .contains(fromMember, toMember, time.nowLocalDateTime)
    }
}