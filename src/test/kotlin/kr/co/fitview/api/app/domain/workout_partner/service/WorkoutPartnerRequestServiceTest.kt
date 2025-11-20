package kr.co.fitview.api.app.domain.workout_partner.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.address.dto.request.AddressCreateServiceRequest
import kr.co.fitview.api.app.domain.address.entity.enums.AddressSiDo
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutExperience
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutGoal
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWorkoutStyle
import kr.co.fitview.api.app.domain.member.entity.enums.WorkoutTimeName
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.member.service.MemberService
import kr.co.fitview.api.app.domain.oauth2.dto.request.OAuth2SignupServiceRequest
import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.domain.workout_partner.condition.WorkoutPartnerRequestCondition
import kr.co.fitview.api.app.domain.workout_partner.dto.request.WorkoutPartnerCreateServiceRequest
import kr.co.fitview.api.app.domain.workout_partner.dto.request.WorkoutPartnerUpdateServiceRequest
import kr.co.fitview.api.app.domain.workout_partner.dto.request.enums.WorkoutPartnerRequestType
import kr.co.fitview.api.app.domain.workout_partner.dto.request.enums.WorkoutPartnerRequestUpdateStatus
import kr.co.fitview.api.app.domain.workout_partner.dto.response.enums.WorkoutPartnerRequestStatusForResponse
import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartner
import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartnerRequest
import kr.co.fitview.api.app.domain.workout_partner.entity.enums.WorkoutPartnerRequestStatus
import kr.co.fitview.api.app.domain.workout_partner.repository.WorkoutPartnerRepository
import kr.co.fitview.api.app.domain.workout_partner.repository.WorkoutPartnerRequestRepository
import kr.co.fitview.api.app.domain.workout_partner.repository.findByOrderedMemberOneIdAndMemberTwoIdAndDeletedAtIsNull
import kr.co.fitview.api.app.global.entity.Gender
import kr.co.fitview.api.app.global.entity.OAuth2Provider
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
import java.time.LocalDate

class WorkoutPartnerRequestServiceTest @Autowired constructor(
    val workoutPartnerRequestService: WorkoutPartnerRequestService,
    val workoutPartnerRepository : WorkoutPartnerRepository,
    val workoutPartnerRequestRepository : WorkoutPartnerRequestRepository,
    val memberService : MemberService,
    val memberRepository : MemberRepository,
    val oAuth2Service : OAuth2Service,
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
        val savedWorkoutPartner = workoutPartnerRequestService.addWorkoutPartnerRequest(savedFromMember.id!!, request)

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
            workoutPartnerRequestService.addWorkoutPartnerRequest(savedFromMember.id!!, request)
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
        val savedWorkoutPartner = workoutPartnerRequestService.addWorkoutPartnerRequest(savedFromMember.id!!, request)

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
        val savedWorkoutPartner = workoutPartnerRequestService.addWorkoutPartnerRequest(savedFromMember.id!!, request)

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
            workoutPartnerRequestService.addWorkoutPartnerRequest(savedFromMember.id!!, request)
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
            workoutPartnerRequestService.addWorkoutPartnerRequest(savedFromMember.id!!, request)
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
        val updatedWorkoutPartnerRequest = workoutPartnerRequestService.updateWorkoutPartnerRequest(
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
        val updatedWorkoutPartnerRequest = workoutPartnerRequestService.updateWorkoutPartnerRequest(
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
            workoutPartnerRequestService.updateWorkoutPartnerRequest(
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
            workoutPartnerRequestService.updateWorkoutPartnerRequest(
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
            workoutPartnerRequestService.updateWorkoutPartnerRequest(
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
            workoutPartnerRequestService.updateWorkoutPartnerRequest(
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

    @DisplayName("최근 운동 파트너 요청을 조회한다.")
    @Test
    fun findRecentRequestWithin24Hours() {
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
        memberService.addMember(fromMember)
        memberService.addMember(toMember)

        val recentWorkoutPartnerRequest = WorkoutPartnerRequest.of(
            fromMember = fromMember,
            toMember = toMember,
            now = time.nowLocalDateTime
        )
        val workoutPartnerRequest = WorkoutPartnerRequest.of(
            fromMember = fromMember,
            toMember = toMember,
            now = time.nowLocalDateTime.minusHours(24)
        )
        workoutPartnerRequestRepository.save(recentWorkoutPartnerRequest)
        workoutPartnerRequestRepository.save(workoutPartnerRequest)

        // when
        val findWorkoutPartnerRequest = workoutPartnerRequestService.findRecentRequestWithin24Hours(fromMember.id!!, toMember.id!!)

        // then
        assertThat(findWorkoutPartnerRequest)
            .extracting("fromMember", "toMember", "status", "requestedAt")
            .contains(fromMember, toMember, WorkoutPartnerRequestStatus.PENDING, time.nowLocalDateTime)
    }

    @DisplayName("최근 운동 파트너 요청을 조회한다. 다만 24시간이 지난 요청은 무시한다.")
    @Test
    fun findRecentRequestWithin24HoursExceed24Hours() {
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
        memberService.addMember(fromMember)
        memberService.addMember(toMember)

        val workoutPartnerRequest = WorkoutPartnerRequest.of(
            fromMember = fromMember,
            toMember = toMember,
            now = time.nowLocalDateTime.minusHours(24)
        )
        workoutPartnerRequestRepository.save(workoutPartnerRequest)

        // when
        val findWorkoutPartnerRequest = workoutPartnerRequestService.findRecentRequestWithin24Hours(fromMember.id!!, toMember.id!!)

        // then
        assertThat(findWorkoutPartnerRequest).isNull()
    }

    fun createOAuth2SignupServiceRequest(
        profileImageUrl: String = "profileImageUrl",
        nickname: String = "nickname",
        gender: Gender = Gender.MALE,
        birthday: LocalDate = LocalDate.of(2000, 1, 1),
        height: Int = 170,
        weight: Int = 65,
        workoutExperience: MemberWorkoutExperience = MemberWorkoutExperience.JUST_STARTED,
        workoutStyle: MemberWorkoutStyle = MemberWorkoutStyle.STRENGTH,
        workoutTimes: List<WorkoutTimeName> = listOf(WorkoutTimeName.WEEKDAY_DAWN, WorkoutTimeName.WEEKDAY_EVENING),
        workoutGoal: MemberWorkoutGoal = MemberWorkoutGoal.PERFORMANCE_GOAL,
        workoutImageUrls: List<String> = listOf(
            "imageUrl1",
            "imageUrl2",
        ),
        intro: String? = "intro",
        address : AddressCreateServiceRequest = AddressCreateServiceRequest(
            siDo = AddressSiDo.SEOUL,
            siGunGu = "강남구",
            eupMyeonDong = "역삼동",
            lat = 37.4979,
            lng = 127.0276,
            fullAddress = "서울특별시 강남구 테헤란로 123"
        )
    ): OAuth2SignupServiceRequest {
        return OAuth2SignupServiceRequest(
            profileImageUrl = profileImageUrl,
            nickname = nickname,
            gender = gender,
            birthday = birthday,
            height = height,
            weight = weight,
            workoutExperience = workoutExperience,
            workoutStyle = workoutStyle,
            workoutTimes = workoutTimes,
            workoutGoal = workoutGoal,
            workoutImageUrls = workoutImageUrls,
            intro = intro,
            address = address
        )
    }

    @DisplayName("운동 파트너 요청 이력을 확인한다.")
    @Test
    fun findWorkoutPartnerFrom() {
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

        val signupRequest = createOAuth2SignupServiceRequest()
        oAuth2Service.signup(signupRequest, me.id!!)
        oAuth2Service.signup(signupRequest, otherMember.id!!)

        val partnerRequest1 = WorkoutPartnerRequest.of(
            fromMember = otherMember,
            toMember = me,
            now = time.nowLocalDateTime.minusHours(5)
        )
        val partnerRequest2 = WorkoutPartnerRequest.of(
            fromMember = otherMember,
            toMember = me,
            now = time.nowLocalDateTime.minusHours(3)
        )
        workoutPartnerRequestRepository.save(partnerRequest1)
        workoutPartnerRequestRepository.save(partnerRequest2)

        val condition = WorkoutPartnerRequestCondition(
            size = 10,
            lastWorkoutPartnerRequestId = null,
            type = WorkoutPartnerRequestType.RECEIVE
        )

        // when
        val slice = workoutPartnerRequestService.findWorkoutPartnerFrom(me.id!!, condition)

        // then
        assertThat(slice.content).hasSize(2)

        val response1 = slice.content[0]
        val response2 = slice.content[1]

        assertThat(response1.workoutPartnerRequestId)
            .isEqualTo(partnerRequest2.id)
        assertThat(response2.workoutPartnerRequestId)
            .isEqualTo(partnerRequest1.id)

        assertThat(response1.nickname).isEqualTo(otherMember.nickname)
        assertThat(response1.profileImageUrl).isEqualTo(signupRequest.profileImageUrl)
        assertThat(response1.workoutExperience).isEqualTo(otherMember.workoutExperience)
        assertThat(response1.workoutGoal).isEqualTo(otherMember.workoutGoal)
        assertThat(response1.workoutStyle).isEqualTo(otherMember.workoutStyle)

        assertThat(response1.status).isEqualTo(WorkoutPartnerRequestStatusForResponse.PENDING)
        assertThat(response2.status).isEqualTo(WorkoutPartnerRequestStatusForResponse.PENDING)

        assertThat(response1.chatRoomId).isNull()
        assertThat(response2.chatRoomId).isNull()

        assertThat(slice.hasNext()).isFalse()
    }

}