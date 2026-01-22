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

class WorkoutCompleteMapperTest @Autowired constructor(
    private val workoutCompleteMapper : WorkoutCompleteMapper,
    private val notificationRepository : NotificationRepository,
    private val memberRepository : MemberRepository,
    private val oAuth2Service : OAuth2Service,
    private val time : Time
) : IntegrationTestSupport(){

    @DisplayName("운동 완료 알림을 조회한다.")
    @Test
    fun map() {
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
                "workoutHistoryId" to 789L,
                "workoutRequestId" to 444L,
                "chatRoomId" to 456L,
                "chatMessageId" to 555L
            )
        )

        val notification = Notification(
            member = member,
            type = NotificationType.WORKOUT_COMPLETE,
            sentAt = time.nowLocalDateTime,
            isRead = false,
            content = content
        )
        notificationRepository.save(notification)

        val response = workoutCompleteMapper.map(notification, member)

        assertThat(response)
            .extracting("notificationId", "type", "sentAt", "isRead", "sender", "link", "messages")
            .contains(
                notification.id!!,
                NotificationType.WORKOUT_COMPLETE,
                notification.sentAt!!,
                notification.isRead!!,
                NotificationSender(123L, "nick", "profile"),
                NotificationLink(
                    LinkType.CHAT_ROOM,
                    mapOf(
                        "workoutHistoryId" to 789L,
                        "chatRoomId" to 456L,
                        "chatMessageId" to 555L
                    )
                ),
                NotificationMessage(
                    text1 = NotificationType.WORKOUT_COMPLETE.displayText1.format(signupRequest.nickname),
                    text2 = NotificationType.WORKOUT_COMPLETE.displayText2.format("nick")
                )
            )
    }
}
