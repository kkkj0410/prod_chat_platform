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

class WorkoutPartnerRequestMapperTest @Autowired constructor(
    private val workoutPartnerRequestMapper : WorkoutPartnerRequestMapper,
    private val notificationRepository : NotificationRepository,
    private val memberRepository : MemberRepository,
    private val oAuth2Service : OAuth2Service,
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
                "memberId" to 123L,
                "workoutPartnerRequestId" to 234L
            )
        )

        val notification = Notification(
            member = member,
            fromMember = fromMember,
            type = NotificationType.WORKOUT_PARTNER_REQUEST,
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

        // when
        val response = workoutPartnerRequestMapper.map(notification, member, sender)


        // then
        assertThat(response)
            .extracting(
                "notificationId",
                "type",
                "sentAt",
                "isRead",
                "sender",
                "link",
                "messages"
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
                ),
                NotificationMessage(
                    text1 = NotificationType.WORKOUT_PARTNER_REQUEST.displayText1.format("nick"),
                    text2 = NotificationType.WORKOUT_PARTNER_REQUEST.displayText2
                )
            )

    }
}