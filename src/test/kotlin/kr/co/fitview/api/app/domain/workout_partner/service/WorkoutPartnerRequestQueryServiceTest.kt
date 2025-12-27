package kr.co.fitview.api.app.domain.workout_partner.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatParticipant
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.chat.repository.ChatMessageRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatParticipantRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatRoomRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequest
import kr.co.fitview.api.app.domain.workout.repository.WorkoutRequestRepository
import kr.co.fitview.api.app.domain.workout_history.entity.WorkoutHistory
import kr.co.fitview.api.app.domain.workout_history.repository.WorkoutHistoryRepository
import kr.co.fitview.api.app.domain.workout_partner.condition.AdminWorkoutPartnerRequestCondition
import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartner
import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartnerRequest
import kr.co.fitview.api.app.domain.workout_partner.entity.enums.WorkoutPartnerRequestContent
import kr.co.fitview.api.app.domain.workout_partner.entity.enums.WorkoutPartnerRequestStatus
import kr.co.fitview.api.app.domain.workout_partner.repository.WorkoutPartnerRepository
import kr.co.fitview.api.app.domain.workout_partner.repository.WorkoutPartnerRequestRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import kr.co.fitview.api.app.global.util.TestDataFactory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class WorkoutPartnerRequestQueryServiceTest @Autowired constructor(
    val workoutPartnerRequestQueryService: WorkoutPartnerRequestQueryService,
    val workoutPartnerRepository : WorkoutPartnerRepository,
    val workoutPartnerRequestRepository: WorkoutPartnerRequestRepository,
    val chatRoomRepository : ChatRoomRepository,
    val chatMessageRepository : ChatMessageRepository,
    val chatParticipantRepository : ChatParticipantRepository,
    val workoutRequestRepository : WorkoutRequestRepository,
    val workoutHistoryRepository: WorkoutHistoryRepository,
    val memberRepository : MemberRepository,
    val oAuth2Service : OAuth2Service,
    val time : Time
) : IntegrationTestSupport(){

    @DisplayName("전체 운동 파트너 요청을 조회한다.")
    @Test
    fun findAllWorkoutPartnerRequestFrom() {
        // given
        val member1 = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val member2 = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val member3 = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(member1)
        memberRepository.save(member2)
        memberRepository.save(member3)

        val signupRequest = TestDataFactory.oAuth2SignupRequest()
        oAuth2Service.signup(signupRequest, member1.id!!)
        oAuth2Service.signup(signupRequest, member2.id!!)
        oAuth2Service.signup(signupRequest, member3.id!!)

        val workoutPartnerRequest1 = WorkoutPartnerRequest.of(
            fromMember = member1,
            toMember = member2,
            now = time.nowLocalDateTime,
            content = WorkoutPartnerRequestContent.BURN
        )
        workoutPartnerRequestRepository.save(workoutPartnerRequest1)

        val workoutPartnerRequest2 = WorkoutPartnerRequest.of(
            fromMember = member2,
            toMember = member1,
            now = time.nowLocalDateTime,
            content = WorkoutPartnerRequestContent.BURN
        )
        workoutPartnerRequestRepository.save(workoutPartnerRequest2)

        val workoutPartnerRequest3 = WorkoutPartnerRequest.of(
            fromMember = member1,
            toMember = member3,
            now = time.nowLocalDateTime,
            content = WorkoutPartnerRequestContent.BURN
        )
        workoutPartnerRequestRepository.save(workoutPartnerRequest3)

        val workoutPartner = WorkoutPartner(
            memberOne = member1,
            memberTwo = member2,
        )
        workoutPartnerRepository.save(workoutPartner)

        val chatRoom = ChatRoom(ChatRoomType.PRIVATE)
        chatRoomRepository.save(chatRoom)

        val chatParticipant1 = ChatParticipant(
            chatRoom = chatRoom,
            member = member1,
        )
        chatParticipantRepository.save(chatParticipant1)

        val chatParticipant2 = ChatParticipant(
            chatRoom = chatRoom,
            member = member2,
        )
        chatParticipantRepository.save(chatParticipant2)

        val chatMessage = ChatMessage.ofWorkoutRequest(
            member = member1,
            chatRoom = chatRoom,
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage)

        val workoutRequest = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = member1,
            toMember = member2,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        workoutRequestRepository.save(workoutRequest)

        val workoutHistory = WorkoutHistory(
            chatRoom = chatRoom,
            workoutRequest = workoutRequest,
            memberOne = member1,
            memberTwo = member2,
            completedAt = time.nowLocalDateTime
        )
        workoutHistoryRepository.save(workoutHistory)

        val condition = AdminWorkoutPartnerRequestCondition()

        // when
        val response = workoutPartnerRequestQueryService.findAllWorkoutPartnerRequestFrom(condition)

        // then
        assertThat(response)
            .extracting(
                "workoutPartnerRequestId",
                "workoutPartnerRequestStatus",
                "respondedAt",
                "hasChatRoom",
                "workoutHistoryCount"
            )
            .contains(
                tuple(
                    workoutPartnerRequest1.id!!,
                    workoutPartnerRequest1.status,
                    workoutPartnerRequest1.respondedAt,
                    true,
                    1L
                ),
                tuple(
                    workoutPartnerRequest2.id!!,
                    workoutPartnerRequest2.status,
                    workoutPartnerRequest2.respondedAt,
                    true,
                    1L
                ),
                tuple(
                    workoutPartnerRequest3.id!!,
                    workoutPartnerRequest3.status,
                    workoutPartnerRequest3.respondedAt,
                    false,
                    0L
                ),
            )
    }

    @DisplayName("본인, 상대방 사이의 상호작용 가능한 파트너 요청을 조회한다.")
    @Test
    fun findActiveRequests() {
        // given
        val meMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val otherMember1 = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val otherMember2 = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(meMember)
        memberRepository.save(otherMember1)
        memberRepository.save(otherMember2)

        val signupRequest = TestDataFactory.oAuth2SignupRequest()
        oAuth2Service.signup(signupRequest, meMember.id!!)
        oAuth2Service.signup(signupRequest, otherMember1.id!!)
        oAuth2Service.signup(signupRequest, otherMember2.id!!)

        val workoutPartnerRequest1 = WorkoutPartnerRequest.of(
            fromMember = meMember,
            toMember = otherMember1,
            now = time.nowLocalDateTime,
            content = WorkoutPartnerRequestContent.BURN
        )
        workoutPartnerRequestRepository.save(workoutPartnerRequest1)

        val workoutPartnerRequest2 = WorkoutPartnerRequest.of(
            fromMember = otherMember2,
            toMember = meMember,
            now = time.nowLocalDateTime,
            content = WorkoutPartnerRequestContent.BURN
        )
        workoutPartnerRequestRepository.save(workoutPartnerRequest2)

        val otherMemberIds = listOf(otherMember1.id!!, otherMember2.id!!)

        // when
        val response = workoutPartnerRequestQueryService.findActiveRequests(
            meMemberId = meMember.id!!,
            otherMemberIds = otherMemberIds
        )

        // then
        assertThat(response).hasSize(2)
        assertThat(response)
            .extracting("id", "fromMember", "toMember")
            .contains(
                tuple(workoutPartnerRequest1.id!!, meMember, otherMember1),
                tuple(workoutPartnerRequest2.id!!, otherMember2, meMember),
            )
    }

    @DisplayName("상호작용 가능한 파트너 요청 조회 시, 상호작용이 불가능한 파트너 요청은 조회하지 않는다.")
    @Test
    fun findActiveRequestsExistsOtherStatus() {
        // given
        val meMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val otherMember1 = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val otherMember2 = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val otherMember3 = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(meMember)
        memberRepository.save(otherMember1)
        memberRepository.save(otherMember2)
        memberRepository.save(otherMember3)

        val signupRequest = TestDataFactory.oAuth2SignupRequest()
        oAuth2Service.signup(signupRequest, meMember.id!!)
        oAuth2Service.signup(signupRequest, otherMember1.id!!)
        oAuth2Service.signup(signupRequest, otherMember2.id!!)

        val workoutPartnerRequest1 = WorkoutPartnerRequest.of(
            fromMember = meMember,
            toMember = otherMember1,
            now = time.nowLocalDateTime,
            content = WorkoutPartnerRequestContent.BURN
        )
        workoutPartnerRequest1.cancel()
        workoutPartnerRequestRepository.save(workoutPartnerRequest1)

        val workoutPartnerRequest2 = WorkoutPartnerRequest.of(
            fromMember = meMember,
            toMember = otherMember2,
            now = time.nowLocalDateTime,
            content = WorkoutPartnerRequestContent.BURN
        )
        workoutPartnerRequest2.reject()
        workoutPartnerRequestRepository.save(workoutPartnerRequest2)

        val workoutPartnerRequest3 = WorkoutPartnerRequest.of(
            fromMember = meMember,
            toMember = otherMember3,
            now = time.nowLocalDateTime,
            content = WorkoutPartnerRequestContent.BURN
        )
        workoutPartnerRequest3.expire()
        workoutPartnerRequestRepository.save(workoutPartnerRequest3)


        val otherMemberIds = listOf(otherMember1.id!!, otherMember2.id!!)

        // when
        val response = workoutPartnerRequestQueryService.findActiveRequests(
            meMemberId = meMember.id!!,
            otherMemberIds = otherMemberIds
        )

        // then
        assertThat(response).hasSize(0)
    }

    @DisplayName("상호작용 가능한 파트너 요청 조회 시, 수락된 파트너 요청은 조회된다.")
    @Test
    fun findActiveRequestsExistsAcceptStatus() {
        // given
        val meMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val otherMember1 = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val otherMember2 = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(meMember)
        memberRepository.save(otherMember1)
        memberRepository.save(otherMember2)

        val signupRequest = TestDataFactory.oAuth2SignupRequest()
        oAuth2Service.signup(signupRequest, meMember.id!!)
        oAuth2Service.signup(signupRequest, otherMember1.id!!)
        oAuth2Service.signup(signupRequest, otherMember2.id!!)

        val workoutPartnerRequest1 = WorkoutPartnerRequest.of(
            fromMember = meMember,
            toMember = otherMember1,
            now = time.nowLocalDateTime,
            content = WorkoutPartnerRequestContent.BURN
        )
        workoutPartnerRequest1.accept()
        workoutPartnerRequestRepository.save(workoutPartnerRequest1)

        val otherMemberIds = listOf(otherMember1.id!!, otherMember2.id!!)

        // when
        val response = workoutPartnerRequestQueryService.findActiveRequests(
            meMemberId = meMember.id!!,
            otherMemberIds = otherMemberIds
        )

        // then
        assertThat(response).hasSize(1)
        assertThat(response[0].id).isEqualTo(workoutPartnerRequest1.id!!)
        assertThat(response[0].status).isEqualTo(WorkoutPartnerRequestStatus.ACCEPT)
    }

    @DisplayName("상호작용 가능한 파트너 요청 조회 시, 운동 요청 대기중이라도 24시간이 지난 요청은 조회되지 않는다.")
    @Test
    fun findActiveRequestsExistsExpireRequest() {
        // given
        val meMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val otherMember1 = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val otherMember2 = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        val otherMember3 = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(meMember)
        memberRepository.save(otherMember1)
        memberRepository.save(otherMember2)
        memberRepository.save(otherMember3)

        val signupRequest = TestDataFactory.oAuth2SignupRequest()
        oAuth2Service.signup(signupRequest, meMember.id!!)
        oAuth2Service.signup(signupRequest, otherMember1.id!!)
        oAuth2Service.signup(signupRequest, otherMember2.id!!)

        val workoutPartnerRequest1 = WorkoutPartnerRequest.of(
            fromMember = meMember,
            toMember = otherMember1,
            now = time.nowLocalDateTime.minusHours(24).minusSeconds(1),
            content = WorkoutPartnerRequestContent.BURN
        )
        workoutPartnerRequestRepository.save(workoutPartnerRequest1)

        val workoutPartnerRequest2 = WorkoutPartnerRequest.of(
            fromMember = meMember,
            toMember = otherMember2,
            now = time.nowLocalDateTime.minusHours(24),
            content = WorkoutPartnerRequestContent.BURN
        )
        workoutPartnerRequestRepository.save(workoutPartnerRequest2)

        val otherMemberIds = listOf(otherMember1.id!!, otherMember2.id!!)

        // when
        val response = workoutPartnerRequestQueryService.findActiveRequests(
            meMemberId = meMember.id!!,
            otherMemberIds = otherMemberIds
        )

        // then
        assertThat(response).hasSize(1)
        assertThat(response[0].id).isEqualTo(workoutPartnerRequest2.id!!)
        assertThat(response[0].status).isEqualTo(WorkoutPartnerRequestStatus.PENDING)
    }


}