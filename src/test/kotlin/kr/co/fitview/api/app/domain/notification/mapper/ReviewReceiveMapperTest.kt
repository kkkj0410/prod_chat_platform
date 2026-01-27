package kr.co.fitview.api.app.domain.notification.mapper

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.notification.dto.response.enums.LinkType
import kr.co.fitview.api.app.domain.notification.dto.response.NotificationLink
import kr.co.fitview.api.app.domain.notification.dto.response.NotificationMessage
import kr.co.fitview.api.app.domain.notification.dto.response.NotificationSender
import kr.co.fitview.api.app.domain.notification.entity.Notification
import kr.co.fitview.api.app.domain.notification.entity.enums.NotificationType
import kr.co.fitview.api.app.domain.notification.repository.NotificationRepository
import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import kr.co.fitview.api.app.global.util.TestDataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ReviewReceiveMapperTest @Autowired constructor(
    private val reviewReceiveMapper : ReviewReceiveMapper,
    private val notificationRepository : NotificationRepository,
    private val memberRepository : MemberRepository,
    private val oAuth2Service : OAuth2Service,
    private val time : Time
) : IntegrationTestSupport(){


    @DisplayName("리뷰 수신 알림을 조회한다.")
    @Test
    fun map() {
        val member = Member(email = "email", password = "password", role = Role.USER)
        memberRepository.save(member)

        val signupRequest = TestDataFactory.oAuth2SignupRequest()
        oAuth2Service.signup(signupRequest, member.id!!)

        val fromMember = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(fromMember)

        val content: MutableMap<String, Any> = mutableMapOf(
            "payload" to mutableMapOf(
                "reviewId" to 456L,
                "workoutHistoryId" to 555L,
                "chatRoomId" to 666L
            )
        )

        val notification = Notification(
            member = member,
            fromMember = fromMember,
            type = NotificationType.REVIEW_RECEIVE,
            sentAt = time.nowLocalDateTime,
            isRead = false,
            content = content
        )
        notificationRepository.save(notification)

        val sender = NotificationSender(
            memberId = 123L,
            nickname = "nick",
            profileImageUrl = "profile"
        )

        val response = reviewReceiveMapper.map(notification, member, sender)

        assertThat(response)
            .extracting("notificationId", "type", "sentAt", "isRead", "sender", "link", "messages")
            .contains(
                notification.id!!,
                NotificationType.REVIEW_RECEIVE,
                notification.sentAt!!,
                notification.isRead!!,
                NotificationSender(123L, "nick", "profile"),
                NotificationLink(LinkType.MEMBER_PROFILE_ME, null),
                NotificationMessage(
                    text1 = NotificationType.REVIEW_RECEIVE.displayText1.format("nick"),
                    text2 = NotificationType.REVIEW_RECEIVE.displayText2
                )

            )
    }
}