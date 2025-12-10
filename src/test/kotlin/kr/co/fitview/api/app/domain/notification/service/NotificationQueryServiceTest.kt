package kr.co.fitview.api.app.domain.notification.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.notification.condition.NotificationCondition
import kr.co.fitview.api.app.domain.notification.dto.response.LinkType
import kr.co.fitview.api.app.domain.notification.entity.Notification
import kr.co.fitview.api.app.domain.notification.entity.enums.NotificationType
import kr.co.fitview.api.app.domain.notification.repository.NotificationRepository
import kr.co.fitview.api.app.domain.notification.repository.NotificationRepositoryTest
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class NotificationQueryServiceTest @Autowired constructor(
    private val memberRepository : MemberRepository,
    private val notificationRepository: NotificationRepository,
    private val notificationQueryService : NotificationQueryService,
    private val time : Time
) : IntegrationTestSupport(){

    @DisplayName("회원이 받은 인앱 알람을 조회한다.")
    @Test
    fun findAllNotificationFrom() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(member)

        val senderData = mapOf(
            "memberId" to 999L,
            "nickname" to "헬스매니아",
            "profileImageUrl" to "https://image.url/profile.jpg"
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
            type = NotificationType.WORKOUT_PARTNER_REQUEST,
            content = contentMap,
            sentAt = time.nowLocalDateTime
        )
        notificationRepository.save(notification)

        val condition = NotificationCondition()

        // when
        val findNotifications = notificationQueryService.findAllNotificationFrom(member.id!!, condition)

        // then
        val response = findNotifications.content
        assertThat(response).hasSize(1)
        assertThat(response[0])
            .extracting("notificationId", "type", "sentAt", "isRead", "link.type", "link.parameters.memberId")
            .contains(
                notification.id!!,
                NotificationType.WORKOUT_PARTNER_REQUEST,
                time.nowLocalDateTime,
                false,
                LinkType.MEMBER_PROFILE,
                999L
            )
    }
}