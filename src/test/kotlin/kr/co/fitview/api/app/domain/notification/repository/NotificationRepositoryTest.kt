package kr.co.fitview.api.app.domain.notification.repository

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.notification.condition.NotificationCondition
import kr.co.fitview.api.app.domain.notification.dto.response.LinkType
import kr.co.fitview.api.app.domain.notification.entity.Notification
import kr.co.fitview.api.app.domain.notification.entity.enums.NotificationType
import kr.co.fitview.api.app.domain.workout_history.entity.QWorkoutHistory.workoutHistory
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class NotificationRepositoryTest @Autowired constructor(
    private val notificationRepository : NotificationRepository,
    private val memberRepository : MemberRepository,
    private val time : Time
) : IntegrationTestSupport(){

    @DisplayName("해당 회원의 리뷰를 조회한다.")
    @Test
    fun findAllNotificationBy() {
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

}