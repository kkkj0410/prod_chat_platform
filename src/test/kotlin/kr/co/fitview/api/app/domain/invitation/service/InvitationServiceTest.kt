package kr.co.fitview.api.app.domain.invitation.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.image.repository.ImageRepository
import kr.co.fitview.api.app.domain.image.repository.MemberImageRepository
import kr.co.fitview.api.app.domain.image.service.ImageService
import kr.co.fitview.api.app.domain.invitation.dto.request.InvitationWorkoutPartnerServiceRequest
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.domain.workout_partner.entity.enums.WorkoutPartnerRequestContent
import kr.co.fitview.api.app.domain.workout_partner.entity.enums.WorkoutPartnerRequestStatus
import kr.co.fitview.api.app.domain.workout_partner.repository.WorkoutPartnerRequestRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.invitation.InvitationErrorCode
import kr.co.fitview.api.app.global.sqids.service.SqidsService
import kr.co.fitview.api.app.global.util.TestDataFactory
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired

class InvitationServiceTest @Autowired constructor(

    private val invitationService: InvitationService,
    private val sqidsService: SqidsService,
    private val memberRepository: MemberRepository,
    private val workoutPartnerRequestRepository: WorkoutPartnerRequestRepository,
    private val oAuth2Service : OAuth2Service

) : IntegrationTestSupport() {

    @DisplayName("운동 파트너 요청과 운동 파트너를 즉시 성립한다.")
    @Test
    fun processWorkoutPartner() {
        // given
        val fromMember = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(fromMember)

        val signupRequest1 = TestDataFactory.oAuth2SignupRequest()
        oAuth2Service.signup(signupRequest1 , fromMember.id!!)

        val toMember = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(toMember)

        val signupRequest2 = TestDataFactory.oAuth2SignupRequest()
        oAuth2Service.signup(signupRequest2, toMember.id!!)

        val toMemberCode = sqidsService.encode(toMember.id!!)

        val request = InvitationWorkoutPartnerServiceRequest(
            invitationCode = toMemberCode
        )

        // when
        val workoutPartner = invitationService.processWorkoutPartner(
            memberId = fromMember.id!!,
            request = request
        )

        // then
        assertThat(workoutPartner)
            .extracting(
                "memberOne.id",
                "memberTwo.id"
            )
            .contains(
                fromMember.id!!,
                toMember.id!!,
            )

        val findWorkoutPartnerRequests = workoutPartnerRequestRepository.findAll()
        assertThat(findWorkoutPartnerRequests).hasSize(1)
        assertThat(findWorkoutPartnerRequests[0])
            .extracting(
                "fromMember.id",
                "toMember.id",
                "status",
                "content"
            )
            .contains(
                fromMember.id!!,
                toMember.id!!,
                WorkoutPartnerRequestStatus.ACCEPT,
                WorkoutPartnerRequestContent.entries[0]
            )

    }

    @DisplayName("운동 파트너 즉시 성립 시, 회원 코드가 유효해야한다.")
    @Test
    fun processWorkoutPartnerUnavailableCode() {
        // given
        val fromMember = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(fromMember)

        val toMemberCode = sqidsService.encode(100L)

        val request = InvitationWorkoutPartnerServiceRequest(
            invitationCode = toMemberCode
        )

        // then
        assertThat(
            assertThrows<GlobalException> {
                invitationService.processWorkoutPartner(
                    memberId = fromMember.id!!,
                    request = request
                )
            }.errorCode
        ).isEqualTo(InvitationErrorCode.INVITATION_MEMBER_NOT_FOUND)
    }

}