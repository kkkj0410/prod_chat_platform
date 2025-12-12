package kr.co.fitview.api.app.domain.notification.mapper

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.notification.dto.response.LinkType
import kr.co.fitview.api.app.domain.notification.dto.response.NotificationLink
import kr.co.fitview.api.app.domain.notification.dto.response.NotificationSender
import kr.co.fitview.api.app.domain.notification.entity.Notification
import kr.co.fitview.api.app.domain.notification.entity.enums.NotificationType
import kr.co.fitview.api.app.domain.notification.repository.NotificationRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class WorkoutPartnerRejectMapperTest @Autowired constructor(
    private val workoutPartnerRejectMapper : WorkoutPartnerRejectMapper,
    private val notificationRepository : NotificationRepository,
    private val memberRepository : MemberRepository,
    private val time : Time
) : IntegrationTestSupport(){

    @DisplayName("운동 파트너 거절 알림을 조회한다.")
    @Test
    fun map() {
        val member = Member(email = "email", password = "password", role = Role.USER)
        memberRepository.save(member)

        val content: MutableMap<String, Any> = mutableMapOf(
            "sender" to mutableMapOf(
                "memberId" to 123L,
                "nickname" to "nick",
                "profileImageUrl" to "profile"
            ),
            "payload" to mutableMapOf(
                "memberId" to 456L,
                "workoutPartnerRequestId" to 666L
            )
        )

        val notification = Notification(
            member = member,
            type = NotificationType.WORKOUT_PARTNER_REJECT,
            sentAt = time.nowLocalDateTime,
            isRead = false,
            content = content
        )
        notificationRepository.save(notification)

        val response = workoutPartnerRejectMapper.map(notification)

        assertThat(response)
            .extracting("notificationId", "type", "sentAt", "isRead", "sender", "link")
            .contains(
                notification.id!!,
                NotificationType.WORKOUT_PARTNER_REJECT,
                notification.sentAt!!,
                notification.isRead!!,
                NotificationSender(123L, "nick", "profile"),
                NotificationLink(LinkType.MEMBER_PROFILE, mapOf("memberId" to 456L))
            )
    }
}