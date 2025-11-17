package kr.co.fitview.api.app.domain.workout_partner.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.service.MemberService
import kr.co.fitview.api.app.domain.workout_partner.dto.request.WorkoutPartnerCreateServiceRequest
import kr.co.fitview.api.app.domain.workout_partner.dto.request.WorkoutPartnerUpdateServiceRequest
import kr.co.fitview.api.app.domain.workout_partner.dto.request.enums.WorkoutPartnerUpdateType
import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartner
import kr.co.fitview.api.app.domain.workout_partner.repository.WorkoutPartnerRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.workout_partner.WorkoutPartnerErrorCode
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.ThrowingConsumer
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class WorkoutPartnerServiceTest @Autowired constructor(
    val workoutPartnerService: WorkoutPartnerService,
    val workoutPartnerRepository : WorkoutPartnerRepository,
    val memberService : MemberService,
    val time : Time
) : IntegrationTestSupport(){

    @DisplayName("회원은 다른 회원에게 핏버디를 요청한다.")
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

        val request = WorkoutPartnerCreateServiceRequest(
            memberId = savedToMember.id!!
        )

        // when
        val savedWorkoutPartner = workoutPartnerService.addWorkoutPartner(savedFromMember.id!!, request)

        // then
        assertThat(savedWorkoutPartner.id).isNotNull()
        assertThat(savedWorkoutPartner)
            .extracting("fromMember", "toMember", "requestedAt")
            .contains(savedFromMember, savedToMember, time.nowLocalDateTime)
    }


    @DisplayName("핏버디 요청 시, 24시간 동안 본인이 상대방에게 요청을 보냈으면 요청 불가")
//    @Test
    fun addWorkoutPartnerWithin24H() {
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

        val workoutPartner = WorkoutPartner(
            savedFromMember,
            savedToMember,
            time.nowLocalDateTime.minusHours(1)
        )
        workoutPartnerRepository.save(workoutPartner)

        val request = WorkoutPartnerCreateServiceRequest(
            memberId = savedToMember.id!!
        )

        // when & then
        assertThatThrownBy {
            workoutPartnerService.addWorkoutPartner(savedFromMember.id!!, request)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(WorkoutPartnerErrorCode.PARTNER_REQUEST_COOLDOWN)
            })
    }

    @DisplayName("핏버디 요청 시, 24시간 이내에 상대방에게 거절당했으면 다시 친구 요청 가능")
//    @Test
    fun addWorkoutPartnerWithin24HAndReject() {
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

        val workoutPartner = WorkoutPartner(
            savedFromMember,
            savedToMember,
            time.nowLocalDateTime
        )
        workoutPartner.reject(time.nowLocalDateTime)
        workoutPartnerRepository.save(workoutPartner)

        val request = WorkoutPartnerCreateServiceRequest(
            memberId = savedToMember.id!!
        )

        // when
        val savedWorkoutPartner = workoutPartnerService.addWorkoutPartner(savedFromMember.id!!, request)

        // then
        assertThat(savedWorkoutPartner.id).isNotNull()
        assertThat(savedWorkoutPartner)
            .extracting("fromMember", "toMember", "requestedAt")
            .contains(savedFromMember, savedToMember, time.nowLocalDateTime)
    }

    @DisplayName("핏버디 요청 시, 24시간 이내에 본인이 취소했으면 다시 친구 요청 가능")
//    @Test
    fun addWorkoutPartnerWithin24HAndCancel() {
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

        val workoutPartner = WorkoutPartner(
            savedFromMember,
            savedToMember,
            time.nowLocalDateTime
        )
        workoutPartner.cancel(time.nowLocalDateTime)
        workoutPartnerRepository.save(workoutPartner)

        val request = WorkoutPartnerCreateServiceRequest(
            memberId = savedToMember.id!!
        )

        // when
        val savedWorkoutPartner = workoutPartnerService.addWorkoutPartner(savedFromMember.id!!, request)

        // then
        assertThat(savedWorkoutPartner.id).isNotNull()
        assertThat(savedWorkoutPartner)
            .extracting("fromMember", "toMember", "requestedAt")
            .contains(savedFromMember, savedToMember, time.nowLocalDateTime)
    }

    @DisplayName("핏버디 요청 시, 이미 수락됐으면 핏버디 요청 불가")
//    @Test
    fun addWorkoutPartnerAccept() {
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

        val workoutPartner = WorkoutPartner(
            savedFromMember,
            savedToMember,
            time.nowLocalDateTime
        )
        workoutPartner.accept(time.nowLocalDateTime)
        workoutPartnerRepository.save(workoutPartner)

        val request = WorkoutPartnerCreateServiceRequest(
            memberId = savedToMember.id!!
        )

        // when & then
        assertThatThrownBy {
            workoutPartnerService.addWorkoutPartner(savedFromMember.id!!, request)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(WorkoutPartnerErrorCode.ALREADY_PARTNER_ACCEPTED)
            })
    }

    @DisplayName("핏버디 요청을 수락한다.")
    @Test
    fun updateWorkoutPartnerAccept() {
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

        val workoutPartner = WorkoutPartner(
            savedFromMember,
            savedToMember,
            time.nowLocalDateTime
        )
        val savedWorkoutPartner = workoutPartnerRepository.save(workoutPartner)

        val request = WorkoutPartnerUpdateServiceRequest(
            type = WorkoutPartnerUpdateType.ACCEPT
        )

        // when
        val updatedWorkoutPartner = workoutPartnerService.updateWorkoutPartner(
            memberId = savedToMember.id!!,
            workoutPartnerId = savedWorkoutPartner.id!!,
            request
        )

        // then
        assertThat(updatedWorkoutPartner.id).isNotNull()
        assertThat(updatedWorkoutPartner)
            .extracting("fromMember", "toMember", "acceptedAt")
            .contains(savedFromMember, savedToMember, time.nowLocalDateTime)
    }

    @DisplayName("핏버디 요청을 거절한다.")
    @Test
    fun updateWorkoutPartnerReject() {
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

        val workoutPartner = WorkoutPartner(
            savedFromMember,
            savedToMember,
            time.nowLocalDateTime
        )
        val savedWorkoutPartner = workoutPartnerRepository.save(workoutPartner)

        val request = WorkoutPartnerUpdateServiceRequest(
            type = WorkoutPartnerUpdateType.REJECT
        )

        // when
        val updatedWorkoutPartner = workoutPartnerService.updateWorkoutPartner(
            memberId = savedToMember.id!!,
            workoutPartnerId = savedWorkoutPartner.id!!,
            request
        )

        // then
        assertThat(updatedWorkoutPartner.id).isNotNull()
        assertThat(updatedWorkoutPartner)
            .extracting("fromMember", "toMember", "rejectedAt")
            .contains(savedFromMember, savedToMember, time.nowLocalDateTime)
    }

    @DisplayName("핏버디 요청이 이미 취소됐으면 핏버디 요청을 받지 못한다.")
    @Test
    fun updateWorkoutPartnerWhenAlreadyCanceled() {
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

        val workoutPartner = WorkoutPartner(
            savedFromMember,
            savedToMember,
            time.nowLocalDateTime
        )
        workoutPartner.cancel(time.nowLocalDateTime)
        val savedWorkoutPartner = workoutPartnerRepository.save(workoutPartner)

        val request = WorkoutPartnerUpdateServiceRequest(
            type = WorkoutPartnerUpdateType.REJECT
        )

        //when & than
        assertThatThrownBy {
            workoutPartnerService.updateWorkoutPartner(
                memberId = savedToMember.id!!,
                workoutPartnerId = savedWorkoutPartner.id!!,
                request
            )
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(WorkoutPartnerErrorCode.PARTNER_REQUEST_ALREADY_FINALIZED)
            })

    }

    @DisplayName("핏버디 요청이 이미 거절됐으면 핏버디 요청을 받지 못한다.")
    @Test
    fun updateWorkoutPartnerWhenAlreadyRejected() {
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

        val workoutPartner = WorkoutPartner(
            savedFromMember,
            savedToMember,
            time.nowLocalDateTime
        )
        workoutPartner.reject(time.nowLocalDateTime)
        val savedWorkoutPartner = workoutPartnerRepository.save(workoutPartner)

        val request = WorkoutPartnerUpdateServiceRequest(
            type = WorkoutPartnerUpdateType.REJECT
        )

        //when & than
        assertThatThrownBy {
            workoutPartnerService.updateWorkoutPartner(
                memberId = savedToMember.id!!,
                workoutPartnerId = savedWorkoutPartner.id!!,
                request
            )
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(WorkoutPartnerErrorCode.PARTNER_REQUEST_ALREADY_FINALIZED)
            })

    }

    @DisplayName("핏버디 요청이 이미 수락됐으면 핏버디 요청을 받지 못한다.")
    @Test
    fun updateWorkoutPartnerWhenAlreadyAccepted() {
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

        val workoutPartner = WorkoutPartner(
            savedFromMember,
            savedToMember,
            time.nowLocalDateTime
        )
        workoutPartner.accept(time.nowLocalDateTime)
        val savedWorkoutPartner = workoutPartnerRepository.save(workoutPartner)

        val request = WorkoutPartnerUpdateServiceRequest(
            type = WorkoutPartnerUpdateType.REJECT
        )

        //when & than
        assertThatThrownBy {
            workoutPartnerService.updateWorkoutPartner(
                memberId = savedToMember.id!!,
                workoutPartnerId = savedWorkoutPartner.id!!,
                request
            )
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(WorkoutPartnerErrorCode.PARTNER_REQUEST_ALREADY_FINALIZED)
            })
    }
}