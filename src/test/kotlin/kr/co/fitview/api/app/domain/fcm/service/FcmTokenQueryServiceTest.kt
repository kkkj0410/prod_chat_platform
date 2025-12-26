package kr.co.fitview.api.app.domain.fcm.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.fcm.constant.DeepLinkConstant
import kr.co.fitview.api.app.domain.fcm.dto.request.*
import kr.co.fitview.api.app.domain.fcm.entity.FcmToken
import kr.co.fitview.api.app.domain.fcm.entity.enums.FcmTokenPlatform
import kr.co.fitview.api.app.domain.fcm.enums.FcmMessage
import kr.co.fitview.api.app.domain.fcm.repository.FcmTokenRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.then
import org.springframework.beans.factory.annotation.Autowired
import java.time.format.DateTimeFormatter

class FcmTokenQueryServiceTest @Autowired constructor(
    private val fcmTokenQueryService : FcmTokenQueryService,
    private val fcmTokenRepository : FcmTokenRepository,
    private val memberRepository : MemberRepository,
    private val deepLinkConstant: DeepLinkConstant,
    private val time : Time,
) : IntegrationTestSupport(){

//    @DisplayName("")
//    @Test
//    fun test() {
//        // given
//        fcmPublisher.send()
//        // when
//
//        // then
//
//    }


    @DisplayName("운동 요청 푸시 알람을 보낸다.")
    @Test
    fun sendWorkoutPartnerRequest() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(member)

        val fcmToken = FcmToken(
            member = member,
            deviceId = "deviceId",
            token = "token",
            isActive = true,
            platform = FcmTokenPlatform.ANDROID
        )
        fcmTokenRepository.save(fcmToken)

        val event = EventFcmWorkoutPartnerRequest(
            toMemberId = member.id!!,
            fromNickname = "nick",
            fromMemberId = 2
        )

        // when
        fcmTokenQueryService.sendWorkoutPartnerRequest(event)

        // then
        then(fcmPublisher).should().send(
            token = "token",
            title = FcmMessage.WORKOUT_PARTNER_REQUEST.title,
            body = FcmMessage.WORKOUT_PARTNER_REQUEST.formatBody("nick"),
            platform = FcmTokenPlatform.ANDROID,
            data = mapOf(
                "type" to "WORKOUT_PARTNER_REQUEST",
                "deepLink" to deepLinkConstant.BASE_DOMAIN + FcmMessage.WORKOUT_PARTNER_REQUEST.formatDeepLinkPath(2)
            )
        )
    }

    private fun saveTestMemberWithFcmToken(): Member {
        val member = Member(email = "email", password = "password", role = Role.USER)
        memberRepository.save(member)

        val fcmToken = FcmToken(
            member = member,
            deviceId = "deviceId",
            token = "token",
            isActive = true,
            platform = FcmTokenPlatform.ANDROID
        )
        fcmTokenRepository.save(fcmToken)

        return member
    }

    @DisplayName("운동 파트너 수락 푸시 알람을 보낸다.")
    @Test
    fun sendWorkoutPartnerAccept() {
        val member = saveTestMemberWithFcmToken()

        val event = EventFcmWorkoutPartnerAccept(
            toMemberId = member.id!!,
            fromMemberId = 2L
        )

        fcmTokenQueryService.sendWorkoutPartnerAccept(event)

        then(fcmPublisher).should().send(
            token = "token",
            title = FcmMessage.WORKOUT_PARTNER_ACCEPT.title,
            body = FcmMessage.WORKOUT_PARTNER_ACCEPT.body,
            platform = FcmTokenPlatform.ANDROID,
            data = mapOf(
                "type" to "WORKOUT_PARTNER_ACCEPT",
                "deepLink" to deepLinkConstant.BASE_DOMAIN + FcmMessage.WORKOUT_PARTNER_ACCEPT.formatDeepLinkPath(2L)
            )
        )
    }

    @DisplayName("채팅 메시지 푸시 알람을 보낸다.")
    @Test
    fun sendChatMessage() {
        val member = saveTestMemberWithFcmToken()

        val event = EventFcmChatMessage(
            toMemberId = member.id!!,
            fromNickname = "nick",
            chatRoomId = 1L,
            chatMessageId = 2L
        )

        fcmTokenQueryService.sendChatMessage(event)

        then(fcmPublisher).should().send(
            token = "token",
            title = FcmMessage.CHAT_MESSAGE.formatTitle("nick"),
            body = FcmMessage.CHAT_MESSAGE.body,
            platform = FcmTokenPlatform.ANDROID,
            data = mapOf(
                "type" to "CHAT_MESSAGE",
                "deepLink" to deepLinkConstant.BASE_DOMAIN + FcmMessage.CHAT_MESSAGE.formatDeepLinkPath(1L),
                "chatMessageId" to 2L
            )
        )
    }

    @DisplayName("운동 요청 푸시 알람을 보낸다.")
    @Test
    fun sendWorkoutRequest() {
        val member = saveTestMemberWithFcmToken()

        val scheduledAt = time.nowLocalDateTime.plusDays(1)
        val event = EventFcmWorkoutRequest(
            toMemberId = member.id!!,
            fromNickname = "nick",
            chatRoomId = 1L,
            chatMessageId = 2L,
            scheduledAt = scheduledAt
        )

        fcmTokenQueryService.sendWorkoutRequest(event)

        then(fcmPublisher).should().send(
            token = "token",
            title = FcmMessage.WORKOUT_REQUEST.formatTitle("nick"),
            body = FcmMessage.WORKOUT_REQUEST.formatBody(scheduledAt.format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))),
            platform = FcmTokenPlatform.ANDROID,
            data = mapOf(
                "type" to "WORKOUT_REQUEST",
                "deepLink" to deepLinkConstant.BASE_DOMAIN + FcmMessage.WORKOUT_REQUEST.formatDeepLinkPath(1L),
                "chatMessageId" to 2L
            )
        )
    }

    @DisplayName("운동 요청 수락 푸시 알람을 보낸다.")
    @Test
    fun sendWorkoutRequestAccept() {
        val member = saveTestMemberWithFcmToken()
        val event = EventFcmWorkoutRequestAccept(
            toMemberId = member.id!!,
            chatRoomId = 1L,
            chatMessageId = 2L
        )

        fcmTokenQueryService.sendWorkoutRequestAccept(event)

        then(fcmPublisher).should().send(
            token = "token",
            title = FcmMessage.WORKOUT_REQUEST_ACCEPT.title,
            body = FcmMessage.WORKOUT_REQUEST_ACCEPT.body,
            platform = FcmTokenPlatform.ANDROID,
            data = mapOf(
                "type" to "WORKOUT_REQUEST_ACCEPT",
                "deepLink" to deepLinkConstant.BASE_DOMAIN + FcmMessage.WORKOUT_REQUEST_ACCEPT.formatDeepLinkPath(1L),
                "chatMessageId" to 2L
            )
        )
    }

    @DisplayName("운동 요청 거절 푸시 알람을 보낸다.")
    @Test
    fun sendWorkoutRequestReject() {
        val member = saveTestMemberWithFcmToken()

        val event = EventFcmWorkoutRequestReject(
            toMemberId = member.id!!,
            chatRoomId = 1L,
            chatMessageId = 2L
        )

        fcmTokenQueryService.sendWorkoutRequestReject(event)

        then(fcmPublisher).should().send(
            token = "token",
            title = FcmMessage.WORKOUT_REQUEST_REJECT.title,
            body = FcmMessage.WORKOUT_REQUEST_REJECT.body,
            platform = FcmTokenPlatform.ANDROID,
            data = mapOf(
                "type" to "WORKOUT_REQUEST_REJECT",
                "deepLink" to deepLinkConstant.BASE_DOMAIN + FcmMessage.WORKOUT_REQUEST_REJECT.formatDeepLinkPath(1L),
                "chatMessageId" to 2L
            )
        )
    }

    @DisplayName("운동 완료 푸시 알람을 보낸다.")
    @Test
    fun sendWorkoutComplete() {
        val member = saveTestMemberWithFcmToken()

        val event = EventFcmWorkoutComplete(
            toMemberId = member.id!!,
            chatRoomId = 1L,
            chatMessageId = 2L
        )

        fcmTokenQueryService.sendWorkoutComplete(event)

        then(fcmPublisher).should().send(
            token = "token",
            title = FcmMessage.WORKOUT_COMPLETE.title,
            body = FcmMessage.WORKOUT_COMPLETE.body,
            platform = FcmTokenPlatform.ANDROID,
            data = mapOf(
                "type" to "WORKOUT_COMPLETE",
                "deepLink" to deepLinkConstant.BASE_DOMAIN + FcmMessage.WORKOUT_COMPLETE.formatDeepLinkPath(1L),
                "chatMessageId" to 2L
            )
        )
    }

    @DisplayName("리뷰 수신 푸시 알람을 보낸다.")
    @Test
    fun sendReviewReceive() {
        val member = saveTestMemberWithFcmToken()

        val event = EventFcmReviewReceive(
            toMemberId = member.id!!
        )

        fcmTokenQueryService.sendReviewReceive(event)

        then(fcmPublisher).should().send(
            token = "token",
            title = FcmMessage.REVIEW_RECEIVE.title,
            body = FcmMessage.REVIEW_RECEIVE.body,
            platform = FcmTokenPlatform.ANDROID,
            data = mapOf(
                "type" to "REVIEW_RECEIVE",
                "deepLink" to deepLinkConstant.BASE_DOMAIN + FcmMessage.REVIEW_RECEIVE.deepLinkPath
            )
        )
    }

    @DisplayName("리뷰 요청 푸시 알람을 보낸다.")
    @Test
    fun sendReviewRequest() {
        val member = saveTestMemberWithFcmToken()

        val event = EventFcmReviewRequest(
            toMemberId = member.id!!,
            workoutHistoryId = 2L
        )

        fcmTokenQueryService.sendReviewRequest(event)

        then(fcmPublisher).should().send(
            token = "token",
            title = FcmMessage.REVIEW_REQUEST.title,
            body = FcmMessage.REVIEW_REQUEST.body,
            platform = FcmTokenPlatform.ANDROID,
            data = mapOf(
                "type" to "REVIEW_REQUEST",
                "deepLink" to deepLinkConstant.BASE_DOMAIN + FcmMessage.REVIEW_REQUEST.formatDeepLinkPath(2L),
            )
        )
    }

    @DisplayName("기기 id의 fcm 토큰을 전체 조회한다.")
    @Test
    fun findAllFcmTokenByDeviceId() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(member)

        val fcmToken1 = FcmToken(
            member = member,
            deviceId = "deviceId",
            token = "token",
            isActive = true,
            platform = FcmTokenPlatform.ANDROID
        )
        val fcmToken2 = FcmToken(
            member = member,
            deviceId = "deviceId",
            token = "token",
            isActive = true,
            platform = FcmTokenPlatform.ANDROID
        )
        val fcmToken3 = FcmToken(
            member = member,
            deviceId = "deviceId2",
            token = "token",
            isActive = true,
            platform = FcmTokenPlatform.ANDROID
        )
        fcmTokenRepository.save(fcmToken1)
        fcmTokenRepository.save(fcmToken2)
        fcmTokenRepository.save(fcmToken3)

        // when
        val response = fcmTokenQueryService.findAllFcmTokenByDeviceId("deviceId")

        // then
        assertThat(response).hasSize(2)
        assertThat(response)
            .extracting("id", "deviceId")
            .contains(
                tuple(fcmToken1.id!!, fcmToken1.deviceId!!),
                tuple(fcmToken2.id!!, fcmToken2.deviceId!!)
            )

    }
}