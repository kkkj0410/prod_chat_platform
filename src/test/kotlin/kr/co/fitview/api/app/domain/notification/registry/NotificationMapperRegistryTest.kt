package kr.co.fitview.api.app.domain.notification.registry

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.notification.dto.response.NotificationLink
import kr.co.fitview.api.app.domain.notification.dto.response.NotificationMessage
import kr.co.fitview.api.app.domain.notification.dto.response.NotificationSender
import kr.co.fitview.api.app.domain.notification.dto.response.enums.LinkType
import kr.co.fitview.api.app.domain.notification.entity.Notification
import kr.co.fitview.api.app.domain.notification.entity.enums.NotificationType
import kr.co.fitview.api.app.domain.notification.mapper.WorkoutRequestRejectMapper
import kr.co.fitview.api.app.domain.notification.repository.NotificationRepository
import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import kr.co.fitview.api.app.global.util.TestDataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class NotificationMapperRegistryTest @Autowired constructor(
    private val notificationMapperRegistry : NotificationMapperRegistry,
    private val notificationRepository : NotificationRepository,
    private val memberRepository : MemberRepository,
    private val oAuth2Service : OAuth2Service,
    private val time : Time
) : IntegrationTestSupport(){

    @DisplayName("인앱 알림의 타입에 맞는 응답 메시지를 생성한다.")
    @Test
    fun map() {
        // given
        val member = Member(email = "email", password = "password", role = Role.USER)
        memberRepository.save(member)

        val signupRequest = TestDataFactory.oAuth2SignupRequest()
        oAuth2Service.signup(signupRequest, member.id!!)

        val content: MutableMap<String, Any> = mutableMapOf(
            "sender" to mutableMapOf(
                "memberId" to 123L,
                "nickname" to "nick",
                "profileImageUrl" to "profile"
            ),
            "payload" to mutableMapOf(
                "chatRoomId" to 456L,
                "workoutRequestId" to 666L,
                "chatMessageId" to 555L
            )
        )

        val notification = Notification(
            member = member,
            type = NotificationType.WORKOUT_REQUEST_REJECT,
            sentAt = time.nowLocalDateTime,
            isRead = false,
            content = content
        )
        notificationRepository.save(notification)

        // when
        val response = notificationMapperRegistry.map(
            notification = notification,
            member = member
        )

        // then
        assertThat(response)
            .extracting("notificationId", "type", "sentAt", "isRead", "sender", "link", "messages")
            .contains(
                notification.id!!,
                NotificationType.WORKOUT_REQUEST_REJECT,
                notification.sentAt!!,
                notification.isRead!!,
                NotificationSender(123L, "nick", "profile"),
                NotificationLink(LinkType.CHAT_ROOM, mapOf("chatRoomId" to 456L, "chatMessageId" to 555L)),
                NotificationMessage(
                    text1 = NotificationType.WORKOUT_REQUEST_REJECT.displayText1.format("nick"),
                    text2 = NotificationType.WORKOUT_REQUEST_REJECT.displayText2
                )
            )
    }
}