package kr.co.fitview.api.app.domain.notification.repository

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.notification.condition.NotificationCondition
import kr.co.fitview.api.app.domain.notification.entity.Notification
import kr.co.fitview.api.app.domain.notification.entity.enums.NotificationType
import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import kr.co.fitview.api.app.global.util.TestDataFactory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class NotificationRepositoryTest @Autowired constructor(
    private val notificationRepository : NotificationRepository,
    private val memberRepository : MemberRepository,
    private val oAuth2Service : OAuth2Service,
    private val time : Time
) : IntegrationTestSupport(){

    @DisplayName("해당 회원의 인앱 알람을 조회한다.")
    @Test
    fun findAllNotificationBy() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(member)

        val fromMember = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(fromMember)

        val senderData = mapOf(
            "memberId" to 999L,
        )

        val payloadData = mapOf(
            "memberId" to 999L,
            "workoutPartnerRequestId" to 100L
        )

        val contentMap: MutableMap<String, Any> = mutableMapOf(
            "sender" to senderData,
            "payload" to payloadData
        )
        val notification = Notification.of(
            member = member,
            fromMember = fromMember,
            type = NotificationType.WORKOUT_PARTNER_REQUEST,
            content = contentMap,
            sentAt = time.nowLocalDateTime
        )
        notificationRepository.save(notification)

        val condition = NotificationCondition()

        //when
        val findNotifications = notificationRepository.findAllNotificationBy(
            memberId = member.id!!,
            condition = condition
        )

        //then
        val response = findNotifications.content
        assertThat(response).hasSize(1)
        assertThat(response[0].id).isNotNull()
        assertThat(response[0])
            .extracting("type", "sentAt", "isRead")
            .contains(
                NotificationType.WORKOUT_PARTNER_REQUEST,
                time.nowLocalDateTime,
                false,
            )
    }

    @DisplayName("인앱 알람 조회 시, 상대 회원이 삭제되면 해당 알람을 조회하지않는다.")
    @Test
    fun findAllNotificationByDeleteFromMember() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(member)

        val fromMember = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        fromMember.delete(time.nowLocalDateTime)
        memberRepository.save(fromMember)

        val senderData = mapOf(
            "memberId" to 999L,
        )

        val payloadData = mapOf(
            "memberId" to 999L,
            "workoutPartnerRequestId" to 100L
        )

        val contentMap: MutableMap<String, Any> = mutableMapOf(
            "sender" to senderData,
            "payload" to payloadData
        )
        val notification = Notification.of(
            member = member,
            fromMember = fromMember,
            type = NotificationType.WORKOUT_PARTNER_REQUEST,
            content = contentMap,
            sentAt = time.nowLocalDateTime
        )
        notificationRepository.save(notification)

        val condition = NotificationCondition()

        //when
        val findNotifications = notificationRepository.findAllNotificationBy(
            memberId = member.id!!,
            condition = condition
        )

        //then
        val response = findNotifications.content
        assertThat(response).hasSize(0)
    }

    @DisplayName("알람 id로 알람을 조회한다.")
    @Test
    fun findByIdAndDeletedAtIsNull() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(member)

        val fromMember = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(fromMember)

        val senderData = mapOf(
            "memberId" to 999L,
        )

        val payloadData = mapOf(
            "memberId" to 999L,
            "workoutPartnerRequestId" to 100L
        )

        val contentMap: MutableMap<String, Any> = mutableMapOf(
            "sender" to senderData,
            "payload" to payloadData
        )
        val notification = Notification.of(
            member = member,
            fromMember = fromMember,
            type = NotificationType.WORKOUT_PARTNER_REQUEST,
            content = contentMap,
            sentAt = time.nowLocalDateTime
        )
        notificationRepository.save(notification)

        // when
        val findNotification = notificationRepository.findByIdAndDeletedAtIsNull(notification.id!!)

        // then
        assertThat(findNotification!!.id!!).isEqualTo(notification.id!!)
    }

    @DisplayName("회원은 인앱 알람을 안읽은 것이 있다.")
    @Test
    fun findNotificationReadBy() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(member)

        val fromMember = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(fromMember)

        val notification = Notification.of(
            member = member,
            fromMember = fromMember,
            type = NotificationType.WORKOUT_PARTNER_REQUEST,
            content = mutableMapOf(),
            sentAt = time.nowLocalDateTime
        )
        notificationRepository.save(notification)

        // when
        val response = notificationRepository.findNotificationReadBy(member.id!!)

        // then
        assertThat(response.isUnreadNotificationExists).isTrue()
    }

    @DisplayName("회원은 인앱 알람을 모두 읽었다.")
    @Test
    fun findNotificationReadByAllRead() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(member)

        val fromMember = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(fromMember)

        val notification = Notification.of(
            member = member,
            fromMember = fromMember,
            type = NotificationType.WORKOUT_PARTNER_REQUEST,
            content = mutableMapOf(),
            sentAt = time.nowLocalDateTime
        )
        val notification2 = Notification.of(
            member = member,
            fromMember = fromMember,
            type = NotificationType.WORKOUT_PARTNER_REQUEST,
            content = mutableMapOf(),
            sentAt = time.nowLocalDateTime
        )

        notification.isRead = true
        notification2.isRead = true

        notificationRepository.save(notification)
        notificationRepository.save(notification2)

        // when
        val response = notificationRepository.findNotificationReadBy(member.id!!)

        // then
        assertThat(response.isUnreadNotificationExists).isFalse()
    }

    @DisplayName("인앱 알람의 모든 프로필을 조회한다.")
    @Test
    fun findNotificationProfilesByMemberIdIn() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(member)

        val signupRequest = TestDataFactory.oAuth2SignupRequest(
            nickname = "nick1",
            profileImageUrl = "profile1"
        )
        oAuth2Service.signup(signupRequest, member.id!!)

        val member2 = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(member2)

        val signupRequest2 = TestDataFactory.oAuth2SignupRequest(
            nickname = "nick2",
            profileImageUrl = "profile2"
        )
        oAuth2Service.signup(signupRequest2, member2.id!!)

        val notInMember3 = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(notInMember3)

        val signupRequest3 = TestDataFactory.oAuth2SignupRequest(
            nickname = "nick3",
            profileImageUrl = "profile3"
        )
        oAuth2Service.signup(signupRequest2, notInMember3.id!!)

        val memberIds = listOf(member.id!!, member2.id!!)

        // when
        val response = notificationRepository.findNotificationProfilesByMemberIdIn(
            memberIds = memberIds
        )

        // then
        assertThat(response).hasSize(2)
        assertThat(response)
            .extracting("memberId", "nickname", "profileImageUrl")
            .contains(
                tuple(member.id!!, "nick1", "profile1"),
                tuple(member2.id!!, "nick2", "profile2"),
            )

    }

}