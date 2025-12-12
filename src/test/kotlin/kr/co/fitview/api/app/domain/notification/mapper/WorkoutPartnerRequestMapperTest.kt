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

class WorkoutPartnerRequestMapperTest @Autowired constructor(
    private val workoutPartnerRequestMapper : WorkoutPartnerRequestMapper,
    private val notificationRepository : NotificationRepository,
    private val memberRepository : MemberRepository,
    private val time : Time
) : IntegrationTestSupport(){


    @DisplayName("운동 파트너 요청 알림을 조회한다.")
    @Test
    fun map() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(member)

        val content: MutableMap<String, Any> = mutableMapOf(
            "sender" to mutableMapOf(
                "memberId" to 123L,
                "nickname" to "nick",
                "profileImageUrl" to "profile"
            ),
            "payload" to mutableMapOf(
                "memberId" to 123L,
                "workoutPartnerRequestId" to 234L
            )
        )

        val notification = Notification(
            member = member,
            type = NotificationType.WORKOUT_PARTNER_REQUEST,
            sentAt = time.nowLocalDateTime,
            isRead = false,
            content = content
        )
        notificationRepository.save(notification)

        // when
        val response = workoutPartnerRequestMapper.map(notification)


        // then
        assertThat(response)
            .extracting(
                "notificationId",
                "type",
                "sentAt",
                "isRead",
                "sender",
                "link"
            )
            .contains(
                notification.id!!,
                NotificationType.WORKOUT_PARTNER_REQUEST,
                notification.sentAt!!,
                notification.isRead!!,
                NotificationSender(
                    memberId = 123L,
                    nickname = "nick",
                    profileImageUrl = "profile"
                ),
                NotificationLink(
                    type = LinkType.MEMBER_PROFILE,
                    parameters = mapOf(
                        "memberId" to 123L,
                    )
                )
            )

    }
}