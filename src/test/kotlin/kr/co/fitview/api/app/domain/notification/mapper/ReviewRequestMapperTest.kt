package kr.co.fitview.api.app.domain.notification.mapper

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.notification.dto.response.enums.LinkType
import kr.co.fitview.api.app.domain.notification.dto.response.NotificationLink
import kr.co.fitview.api.app.domain.notification.dto.response.NotificationSender
import kr.co.fitview.api.app.domain.notification.entity.Notification
import kr.co.fitview.api.app.domain.notification.entity.enums.NotificationType
import kr.co.fitview.api.app.domain.notification.repository.NotificationRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ReviewRequestMapperTest @Autowired constructor(
    private val reviewRequestMapper: ReviewRequestMapper,
    private val notificationRepository : NotificationRepository,
    private val memberRepository : MemberRepository,
    private val time : Time
) : IntegrationTestSupport(){


    @DisplayName("리뷰 요청 알림을 조회한다.")
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
                "workoutHistoryId" to 789L,
                "chatRoomId" to 456L
            )
        )

        val notification = Notification(
            member = member,
            type = NotificationType.REVIEW_REQUEST,
            sentAt = time.nowLocalDateTime,
            isRead = false,
            content = content
        )
        notificationRepository.save(notification)

        val response = reviewRequestMapper.map(notification)

        assertThat(response)
            .extracting("notificationId", "type", "sentAt", "isRead", "sender", "link")
            .contains(
                notification.id!!,
                NotificationType.REVIEW_REQUEST,
                notification.sentAt!!,
                notification.isRead!!,
                NotificationSender(123L, "nick", "profile"),
                NotificationLink(
                    LinkType.REVIEW_WRITE,
                    mapOf(
                        "workoutHistoryId" to 789L,
                        "chatRoomId" to 456L
                    )
                )
            )
    }
}