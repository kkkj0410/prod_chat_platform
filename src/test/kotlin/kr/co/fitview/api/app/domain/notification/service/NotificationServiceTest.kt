package kr.co.fitview.api.app.domain.notification.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.notification.dto.request.*
import kr.co.fitview.api.app.domain.notification.entity.enums.NotificationType
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class NotificationServiceTest @Autowired constructor(
    private val memberRepository : MemberRepository,
    private val notificationService : NotificationService,
    private val time : Time
) : IntegrationTestSupport(){


    private fun createMember() = Member(email = "email", password = "password", role = Role.USER).also {
        memberRepository.save(it)
    }

    @DisplayName("알람 메시지를 읽음 여부 처리한다.")
    @Test
    fun modifyNotificationRead() {
        // given
        val member = createMember()

        val fromMember = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(fromMember)

        val event = EventWorkoutPartnerRequest(
            memberId = member.id!!,
            sender = EventSender(
                memberId = fromMember.id!!,
            ),
            payload = EventWorkoutPartnerRequestPayload(
                memberId = 123L,
                workoutPartnerRequestId = 345L
            )
        )

        val savedNotification = notificationService.saveWorkoutPartnerRequest(event)

        // when
        val updatedNotification = notificationService.modifyNotificationRead(savedNotification.id!!)

        // then
        assertThat(updatedNotification.isRead).isEqualTo(true)
    }

    @DisplayName("운동 파트너 요청 알람을 저장한다.")
    @Test
    fun saveWorkoutPartnerRequest() {
        // given
        val member = createMember()

        val fromMember = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(fromMember)

        val event = EventWorkoutPartnerRequest(
            memberId = member.id!!,
            sender = EventSender(
                memberId = fromMember.id!!
            ),
            payload = EventWorkoutPartnerRequestPayload(
                memberId = 123L,
                workoutPartnerRequestId = 345L
            )
        )

        // when
        val savedNotification = notificationService.saveWorkoutPartnerRequest(event)

        // then
        assertThat(savedNotification.id).isNotNull()

        val expectedContent = mapOf(
            "payload" to mapOf(
                "memberId" to 123L,
                "workoutPartnerRequestId" to 345L
            )
        )

        assertThat(savedNotification)
            .extracting("member", "type", "isRead", "content", "sentAt")
            .contains(
                member,
                NotificationType.WORKOUT_PARTNER_REQUEST,
                false,
                expectedContent,
                time.nowLocalDateTime
            )
    }

    @DisplayName("운동 파트너 수락 알람을 저장한다.")
    @Test
    fun saveWorkoutPartnerAccept() {
        //given
        val member = createMember()

        val fromMember = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(fromMember)

        val event = EventWorkoutPartnerAccept(
            memberId = member.id!!,
            sender = EventSender(fromMember.id!!),
            payload = EventWorkoutPartnerAcceptPayload(123L, 345L, 678L)
        )

        //when
        val savedNotification = notificationService.saveWorkoutPartnerAccept(event)

        //then
        assertThat(savedNotification.id).isNotNull()

        val expectedContent = mapOf(
            "payload" to mapOf("workoutPartnerRequestId" to 123L, "workoutPartnerId" to 345L, "memberId" to 678L)
        )

        assertThat(savedNotification)
            .extracting("member", "type", "isRead", "content", "sentAt")
            .contains(member, NotificationType.WORKOUT_PARTNER_ACCEPT, false, expectedContent, time.nowLocalDateTime)
    }

    @DisplayName("운동 파트너 거절 알람을 저장한다.")
    @Test
    fun saveWorkoutPartnerReject() {
        //given
        val member = createMember()

        val fromMember = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(fromMember)

        val event = EventWorkoutPartnerReject(
            memberId = member.id!!,
            sender = EventSender(fromMember.id!!),
            payload = EventWorkoutPartnerRejectPayload(123L, 345L)
        )

        //when
        val savedNotification = notificationService.saveWorkoutPartnerReject(event)

        //then
        assertThat(savedNotification.id).isNotNull()

        val expectedContent = mapOf(
            "payload" to mapOf("workoutPartnerRequestId" to 123L, "memberId" to 345L)
        )

        assertThat(savedNotification)
            .extracting("member", "type", "isRead", "content", "sentAt")
            .contains(member, NotificationType.WORKOUT_PARTNER_REJECT, false, expectedContent, time.nowLocalDateTime)
    }

    @DisplayName("운동 약속 요청 알람을 저장한다.")
    @Test
    fun saveWorkoutRequest() {
        //given
        val member = createMember()

        val fromMember = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(fromMember)


        val event = EventWorkoutRequest(
            memberId = member.id!!,
            sender = EventSender(fromMember.id!!),
            payload = EventWorkoutRequestPayload(
                chatMessageId = 333L,
                workoutRequestId = 345L,
                chatRoomId = 678L
            )
        )

        //when
        val savedNotification = notificationService.saveWorkoutRequest(event)

        //then
        assertThat(savedNotification.id).isNotNull()

        val expectedContent = mapOf(
            "payload" to mapOf("chatMessageId" to 333L, "workoutRequestId" to 345L, "chatRoomId" to 678L)
        )

        assertThat(savedNotification)
            .extracting("member", "type", "isRead", "content", "sentAt")
            .contains(member, NotificationType.WORKOUT_REQUEST, false, expectedContent, time.nowLocalDateTime)
    }

    @DisplayName("운동 약속 수락 알람을 저장한다.")
    @Test
    fun saveWorkoutRequestAccept() {
        //given
        val member = createMember()

        val fromMember = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(fromMember)

        val event = EventWorkoutRequestAccept(
            memberId = member.id!!,
            sender = EventSender(fromMember.id!!),
            payload = EventWorkoutRequestAcceptPayload(
                chatMessageId = 333L,
                workoutRequestId = 345L,
                chatRoomId = 678L
            )
        )

        //when
        val savedNotification = notificationService.saveWorkoutRequestAccept(event)

        //then
        assertThat(savedNotification.id).isNotNull()

        val expectedContent = mapOf(
            "payload" to mapOf("chatMessageId" to 333L, "workoutRequestId" to 345L, "chatRoomId" to 678L)
        )

        assertThat(savedNotification)
            .extracting("member", "type", "isRead", "content", "sentAt")
            .contains(member, NotificationType.WORKOUT_REQUEST_ACCEPT, false, expectedContent, time.nowLocalDateTime)
    }

    @DisplayName("운동 약속 거절 알람을 저장한다.")
    @Test
    fun saveWorkoutRequestReject() {
        //given
        val member = createMember()

        val fromMember = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(fromMember)

        val event = EventWorkoutRequestReject(
            memberId = member.id!!,
            sender = EventSender(fromMember.id!!),
            payload = EventWorkoutRequestRejectPayload(
                chatMessageId = 555L,
                workoutRequestId = 345L,
                chatRoomId = 678L
            )
        )


        //when
        val savedNotification = notificationService.saveWorkoutRequestReject(event)

        //then
        assertThat(savedNotification.id).isNotNull()

        val expectedContent = mapOf(
            "payload" to mapOf("chatMessageId" to 555L, "workoutRequestId" to 345L, "chatRoomId" to 678L)
        )

        assertThat(savedNotification)
            .extracting("member", "type", "isRead", "content", "sentAt")
            .contains(member, NotificationType.WORKOUT_REQUEST_REJECT, false, expectedContent, time.nowLocalDateTime)
    }

    @DisplayName("운동 약속 취소 알람을 저장한다.")
    @Test
    fun saveWorkoutRequestCancel() {
        //given
        val member = createMember()

        val fromMember = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(fromMember)

        val event = EventWorkoutRequestCancel(
            memberId = member.id!!,
            sender = EventSender(fromMember.id!!),
            payload = EventWorkoutRequestCancelPayload(
                chatMessageId = 555L,
                workoutRequestId = 345L,
                chatRoomId = 678L
            )
        )


        //when
        val savedNotification = notificationService.saveWorkoutRequestCancel(event)

        //then
        assertThat(savedNotification.id).isNotNull()

        val expectedContent = mapOf(
            "payload" to mapOf("chatMessageId" to 555L, "workoutRequestId" to 345L, "chatRoomId" to 678L)
        )

        assertThat(savedNotification)
            .extracting("member", "type", "isRead", "content", "sentAt")
            .contains(member, NotificationType.WORKOUT_REQUEST_CANCEL, false, expectedContent, time.nowLocalDateTime)
    }


    @DisplayName("운동 완료 알람을 저장한다.")
    @Test
    fun saveWorkoutComplete() {
        //given
        val member = createMember()

        val fromMember = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(fromMember)

        val event = EventWorkoutComplete(
            memberId = member.id!!,
            sender = EventSender(fromMember.id!!),
            payload = EventWorkoutCompletePayload(
                chatMessageId = 555L,
                workoutRequestId = 345L,
                workoutHistoryId = 678L,
                chatRoomId = 901L
            )
        )


        //when
        val savedNotification = notificationService.saveWorkoutComplete(event)

        //then
        assertThat(savedNotification.id).isNotNull()

        val expectedContent = mapOf(
            "payload" to mapOf("chatMessageId" to 555L,"workoutRequestId" to 345L, "workoutHistoryId" to 678L, "chatRoomId" to 901L)
        )

        assertThat(savedNotification)
            .extracting("member", "type", "isRead", "content", "sentAt")
            .contains(member, NotificationType.WORKOUT_COMPLETE, false, expectedContent, time.nowLocalDateTime)
    }

    @DisplayName("후기 받음 알람을 저장한다.")
    @Test
    fun saveReviewReceive() {
        //given
        val member = createMember()

        val fromMember = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(fromMember)

        val event = EventReviewReceive(
            memberId = member.id!!,
            sender = EventSender(fromMember.id!!),
            payload = EventReviewReceivePayload(345L, 678L, 901L)
        )

        //when
        val savedNotification = notificationService.saveReviewReceive(event)

        //then
        assertThat(savedNotification.id).isNotNull()

        val expectedContent = mapOf(
            "payload" to mapOf("reviewId" to 345L, "workoutHistoryId" to 678L, "chatRoomId" to 901L)
        )

        assertThat(savedNotification)
            .extracting("member", "type", "isRead", "content", "sentAt")
            .contains(member, NotificationType.REVIEW_RECEIVE, false, expectedContent, time.nowLocalDateTime)
    }

    @DisplayName("후기 미작성 알람을 저장한다.")
    @Test
    fun saveReviewRequest() {
        //given
        val member = createMember()

        val fromMember = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(fromMember)

        val event = EventReviewRequest(
            memberId = member.id!!,
            sender = EventSender(fromMember.id!!),
            payload = EventReviewRequestPayload(1L, 2L, 3L)
        )

        //when
        val savedNotification = notificationService.saveReviewRequest(event)

        //then
        assertThat(savedNotification.id).isNotNull()

        val expectedContent = mapOf(
            "payload" to mapOf("workoutHistoryId" to 1L, "chatMessageId" to 2L, "chatRoomId" to 3L)
        )

        assertThat(savedNotification)
            .extracting("member", "type", "isRead", "content", "sentAt")
            .contains(member, NotificationType.REVIEW_REQUEST, false, expectedContent, time.nowLocalDateTime)
    }
}