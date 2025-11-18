package kr.co.fitview.api.app.domain.workout_partner.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.service.MemberService
import kr.co.fitview.api.app.domain.workout_partner.dto.request.WorkoutPartnerCreateServiceRequest
import kr.co.fitview.api.app.domain.workout_partner.dto.request.WorkoutPartnerUpdateServiceRequest
import kr.co.fitview.api.app.domain.workout_partner.dto.request.enums.WorkoutPartnerRequestUpdateStatus
import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartner
import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartnerRequest
import kr.co.fitview.api.app.domain.workout_partner.entity.enums.WorkoutPartnerRequestStatus
import kr.co.fitview.api.app.domain.workout_partner.repository.WorkoutPartnerRepository
import kr.co.fitview.api.app.domain.workout_partner.repository.WorkoutPartnerRequestRepository
import kr.co.fitview.api.app.domain.workout_partner.repository.findByOrderedMemberOneIdAndMemberTwoIdAndDeletedAtIsNull
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
    val workoutPartnerRequestRepository : WorkoutPartnerRequestRepository,
    val memberService : MemberService,
    val time : Time
) : IntegrationTestSupport(){

    @DisplayName("회원은 다른 회원에게 핏버디를 요청한다.")
    @Test
    fun addWorkoutPartnerRequest() {
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
        val savedWorkoutPartner = workoutPartnerService.addWorkoutPartnerRequest(savedFromMember.id!!, request)

        // then
        assertThat(savedWorkoutPartner.id).isNotNull()
        assertThat(savedWorkoutPartner)
            .extracting("fromMember", "toMember", "requestedAt")
            .contains(savedFromMember, savedToMember, time.nowLocalDateTime)
    }


    @DisplayName("핏버디 요청 시, 24시간 동안 본인이 상대방에게 요청을 보냈으면 요청 불가")
//    @Test
    fun addWorkoutPartnerRequestWithin24H() {
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
            now = time.nowLocalDateTime.minusHours(1)
        )
        workoutPartnerRequestRepository.save(workoutPartnerRequest)

        val request = WorkoutPartnerCreateServiceRequest(
            memberId = savedToMember.id!!
        )

        // when & then
        assertThatThrownBy {
            workoutPartnerService.addWorkoutPartnerRequest(savedFromMember.id!!, request)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(WorkoutPartnerErrorCode.PARTNER_REQUEST_COOLDOWN)
            })
    }

    @DisplayName("핏버디 요청 시, 24시간 이내에 상대방에게 거절당했으면 다시 친구 요청 가능")
    @Test
    fun addWorkoutPartnerRequestWithin24HAndReject() {
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
            now = time.nowLocalDateTime
        )
        workoutPartnerRequest.reject()
        workoutPartnerRequestRepository.save(workoutPartnerRequest)

        val request = WorkoutPartnerCreateServiceRequest(
            memberId = savedToMember.id!!
        )

        // when
        val savedWorkoutPartner = workoutPartnerService.addWorkoutPartnerRequest(savedFromMember.id!!, request)

        // then
        assertThat(savedWorkoutPartner.id).isNotNull()
        assertThat(savedWorkoutPartner)
            .extracting("fromMember", "toMember", "requestedAt")
            .contains(savedFromMember, savedToMember, time.nowLocalDateTime)
    }

    @DisplayName("핏버디 요청 시, 24시간 이내에 본인이 취소했으면 다시 친구 요청 가능")
    @Test
    fun addWorkoutPartnerRequestWithin24HAndCancel() {
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
            now = time.nowLocalDateTime
        )
        workoutPartnerRequest.cancel()
        workoutPartnerRequestRepository.save(workoutPartnerRequest)

        val request = WorkoutPartnerCreateServiceRequest(
            memberId = savedToMember.id!!
        )

        // when
        val savedWorkoutPartner = workoutPartnerService.addWorkoutPartnerRequest(savedFromMember.id!!, request)

        // then
        assertThat(savedWorkoutPartner.id).isNotNull()
        assertThat(savedWorkoutPartner)
            .extracting("fromMember", "toMember", "requestedAt")
            .contains(savedFromMember, savedToMember, time.nowLocalDateTime)
    }

    @DisplayName("핏버디 요청 시, 이미 수락됐으면 핏버디 요청 불가")
//    @Test
    fun addWorkoutPartnerRequestAccept() {
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
            now = time.nowLocalDateTime
        )
        workoutPartnerRequest.accept()
        workoutPartnerRequestRepository.save(workoutPartnerRequest)

        val request = WorkoutPartnerCreateServiceRequest(
            memberId = savedToMember.id!!
        )

        // when & then
        assertThatThrownBy {
            workoutPartnerService.addWorkoutPartnerRequest(savedFromMember.id!!, request)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(WorkoutPartnerErrorCode.ALREADY_PARTNER_ACCEPTED)
            })
    }

    @DisplayName("이미 핏버디 관계이면 핏버디 요청을 하지 못한다.")
    @Test
    fun addWorkoutPartnerRequestWhenAlreadyWorkoutPartner() {
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

        val workoutPartner = WorkoutPartner.of(
            memberOne = savedFromMember,
            memberTwo = savedToMember
        )
        workoutPartnerRepository.save(workoutPartner)

        val request = WorkoutPartnerCreateServiceRequest(
            memberId = savedToMember.id!!
        )

        // when & then
        assertThatThrownBy {
            workoutPartnerService.addWorkoutPartnerRequest(savedFromMember.id!!, request)
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
    fun updateWorkoutPartnerRequestAccept() {
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
            now = time.nowLocalDateTime
        )
        val savedWorkoutPartnerRequest = workoutPartnerRequestRepository.save(workoutPartnerRequest)

        val request = WorkoutPartnerUpdateServiceRequest(
            type = WorkoutPartnerRequestUpdateStatus.ACCEPT
        )

        // when
        val updatedWorkoutPartnerRequest = workoutPartnerService.updateWorkoutPartnerRequest(
            memberId = savedToMember.id!!,
            workoutPartnerRequestId = savedWorkoutPartnerRequest.id!!,
            request
        )

        // then
        val findWorkoutPartner = workoutPartnerRepository.findByOrderedMemberOneIdAndMemberTwoIdAndDeletedAtIsNull(savedFromMember.id!!, savedToMember.id!!)
        assertThat(findWorkoutPartner)
            .extracting("memberOne", "memberTwo")
            .contains(savedFromMember, savedToMember)

        assertThat(updatedWorkoutPartnerRequest.id).isNotNull()
        assertThat(updatedWorkoutPartnerRequest)
            .extracting("fromMember", "toMember", "status")
            .contains(savedFromMember, savedToMember, WorkoutPartnerRequestStatus.ACCEPT)
    }

    @DisplayName("핏버디 요청을 거절한다.")
    @Test
    fun updateWorkoutPartnerRequestReject() {
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
            now = time.nowLocalDateTime
        )
        val savedWorkoutPartnerRequest = workoutPartnerRequestRepository.save(workoutPartnerRequest)

        val request = WorkoutPartnerUpdateServiceRequest(
            type = WorkoutPartnerRequestUpdateStatus.REJECT
        )

        // when
        val updatedWorkoutPartnerRequest = workoutPartnerService.updateWorkoutPartnerRequest(
            memberId = savedToMember.id!!,
            workoutPartnerRequestId = savedWorkoutPartnerRequest.id!!,
            request
        )

        // then
        assertThat(updatedWorkoutPartnerRequest.id).isNotNull()
        assertThat(updatedWorkoutPartnerRequest)
            .extracting("fromMember", "toMember", "status")
            .contains(savedFromMember, savedToMember, WorkoutPartnerRequestStatus.REJECT)
    }

    @DisplayName("핏버디 요청이 이미 취소됐으면 핏버디 요청을 받지 못한다.")
    @Test
    fun updateWorkoutPartnerRequestWhenAlreadyCanceled() {
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
            now = time.nowLocalDateTime
        )
        workoutPartnerRequest.cancel()
        val savedWorkoutPartnerRequest = workoutPartnerRequestRepository.save(workoutPartnerRequest)

        val request = WorkoutPartnerUpdateServiceRequest(
            type = WorkoutPartnerRequestUpdateStatus.REJECT
        )

        //when & than
        assertThatThrownBy {
            workoutPartnerService.updateWorkoutPartnerRequest(
                memberId = savedToMember.id!!,
                workoutPartnerRequestId = savedWorkoutPartnerRequest.id!!,
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
    fun updateWorkoutPartnerRequestWhenAlreadyRejected() {
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
            now = time.nowLocalDateTime
        )
        workoutPartnerRequest.reject()
        val savedWorkoutPartnerRequest = workoutPartnerRequestRepository.save(workoutPartnerRequest)

        val request = WorkoutPartnerUpdateServiceRequest(
            type = WorkoutPartnerRequestUpdateStatus.REJECT
        )

        //when & than
        assertThatThrownBy {
            workoutPartnerService.updateWorkoutPartnerRequest(
                memberId = savedToMember.id!!,
                workoutPartnerRequestId = savedWorkoutPartnerRequest.id!!,
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
    fun updateWorkoutPartnerRequestWhenAlreadyAccepted() {
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
            now = time.nowLocalDateTime
        )

        workoutPartnerRequest.accept()
        val savedWorkoutPartner = workoutPartnerRequestRepository.save(workoutPartnerRequest)

        val request = WorkoutPartnerUpdateServiceRequest(
            type = WorkoutPartnerRequestUpdateStatus.REJECT
        )

        //when & than
        assertThatThrownBy {
            workoutPartnerService.updateWorkoutPartnerRequest(
                memberId = savedToMember.id!!,
                workoutPartnerRequestId = savedWorkoutPartner.id!!,
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

    @DisplayName("이미 핏버디 관계이면 핏버디 요청에 대한 응답을 하지 못한다.")
    @Test
    fun updateWorkoutPartnerRequestWhenAlreadyWorkoutPartner() {
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

        val workoutPartner = WorkoutPartner.of(
            memberOne = savedFromMember,
            memberTwo = savedToMember,
        )
        workoutPartnerRepository.save(workoutPartner)

        val workoutPartnerRequest = WorkoutPartnerRequest.of(
            fromMember = savedFromMember,
            toMember = savedToMember,
            now = time.nowLocalDateTime
        )
        val savedWorkoutPartner = workoutPartnerRequestRepository.save(workoutPartnerRequest)

        val request = WorkoutPartnerUpdateServiceRequest(
            type = WorkoutPartnerRequestUpdateStatus.REJECT
        )

        //when & than
        assertThatThrownBy {
            workoutPartnerService.updateWorkoutPartnerRequest(
                memberId = savedToMember.id!!,
                workoutPartnerRequestId = savedWorkoutPartner.id!!,
                request
            )
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(WorkoutPartnerErrorCode.ALREADY_PARTNER_ACCEPTED)
            })

    }
}