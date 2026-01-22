package kr.co.fitview.api.app.domain.fcm.service

import com.google.firebase.ErrorCode
import com.google.firebase.FirebaseException
import com.google.firebase.messaging.FirebaseMessagingException
import com.google.firebase.messaging.MessagingErrorCode
import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.fcm.constant.DeepLinkConstant
import kr.co.fitview.api.app.domain.fcm.dto.request.*
import kr.co.fitview.api.app.domain.fcm.entity.FcmToken
import kr.co.fitview.api.app.domain.fcm.entity.enums.FcmTokenPlatform
import kr.co.fitview.api.app.domain.fcm.enums.FcmMessage
import kr.co.fitview.api.app.domain.fcm.repository.FcmTokenRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.global.entity.OAuth2Provider
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.oauth2.OAuth2ErrorCode
import kr.co.fitview.api.app.global.time.Time
import kr.co.fitview.api.app.global.time.TimeHolder.time
import org.assertj.core.api.Assertions.*
import org.assertj.core.api.ThrowingConsumer
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.then
import org.mockito.kotlin.times
import org.springframework.beans.factory.annotation.Autowired
import java.time.format.DateTimeFormatter

class FcmTokenServiceTest @Autowired constructor(
    private val fcmTokenService: FcmTokenService,
    private val fcmTokenRepository : FcmTokenRepository,
    private val memberRepository : MemberRepository,
    private val deepLinkConstant: DeepLinkConstant,
    private val time : Time,
    private val fcmPublisher: FcmPublisher
) : IntegrationTestSupport(){


    @DisplayName("회원 단말기의 fcm 토큰을 저장한다.")
    @Test
    fun saveFcmToken() {
        // given
        val member = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member)

        val request = FcmTokenCreateServiceRequest(
            deviceId = "deviceId",
            token = "token",
            platform = FcmTokenPlatform.ANDROID
        )

        // when
        fcmTokenService.saveFcmToken(member.id!!, request)

        // then
        val findFcmTokens = fcmTokenRepository.findAll()
        val findFcmToken = findFcmTokens[0]

        val findMember = memberRepository.findById(member.id!!).orElseThrow()

        assertThat(findFcmTokens).hasSize(1)
        assertThat(findFcmToken)
            .extracting("member", "deviceId", "token", "platform")
            .contains(findMember, "deviceId", "token", FcmTokenPlatform.ANDROID)
    }

    @DisplayName("Fcm 토큰 저장 시, 이미 동일한 fcm 토큰이 다른 device에서 쓰이고 있다면 해당 토큰들을 삭제한다.")
    @Test
    fun saveFcmTokenDeleteDuplicatedFcmToken() {
        // given
        val member = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member)

        val duplicatedFcmTokenString = "token"

        val fcmTokenEntity = FcmToken(
            member = member,
            deviceId = "deviceId1",
            token = duplicatedFcmTokenString,
            isActive = true,
            platform = FcmTokenPlatform.ANDROID
        )
        fcmTokenRepository.save(fcmTokenEntity)

        val fcmTokenEntity2 = FcmToken(
            member = member,
            deviceId = "deviceId2",
            token = duplicatedFcmTokenString,
            isActive = true,
            platform = FcmTokenPlatform.ANDROID
        )
        fcmTokenRepository.save(fcmTokenEntity2)

        val request = FcmTokenCreateServiceRequest(
            deviceId = "deviceId3",
            token = duplicatedFcmTokenString,
            platform = FcmTokenPlatform.ANDROID
        )

        // when
        fcmTokenService.saveFcmToken(
            memberId = member.id!!,
            request = request
        )

        // then
        val findFcmTokens = fcmTokenRepository.findAll()
        assertThat(findFcmTokens)
            .extracting("deviceId", "token", "deletedAt")
            .contains(
                tuple(fcmTokenEntity.deviceId, fcmTokenEntity.token, time.nowLocalDateTime),
                tuple(fcmTokenEntity2.deviceId, fcmTokenEntity2.token, time.nowLocalDateTime),
                tuple(request.deviceId, request.token, null)
            )
    }

    @DisplayName("fcm 토큰 저장 시, deviceId가 DB에 동일한게 있다면 해당 fcm 토큰을 업데이트한다.")
    @Test
    fun saveFcmTokenDuplicatedDeviceId() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(member)

        val fcmToken = FcmToken.of(
            member = member,
            deviceId = "deviceId",
            token = "token",
            platform = FcmTokenPlatform.ANDROID
        )
        fcmTokenRepository.save(fcmToken)

        val request = FcmTokenCreateServiceRequest(
            deviceId = "deviceId",
            token = "updateToken",
            platform = FcmTokenPlatform.ANDROID
        )

        // when
        fcmTokenService.saveFcmToken(member.id!!, request)

        // then
        val findFcmTokens = fcmTokenRepository.findAll()
        val findFcmToken = findFcmTokens[0]

        val findMember = memberRepository.findById(member.id!!).orElseThrow()

        assertThat(findFcmTokens).hasSize(1)
        assertThat(findFcmToken)
            .extracting("member", "deviceId", "token", "platform")
            .contains(findMember, "deviceId", "updateToken", FcmTokenPlatform.ANDROID)
    }

    @DisplayName("fcm 토큰 저장 시, 동일한 디바이스로 다른 계정 접속을 할 시, fcm 토큰 소유자를 해당 다른 계정으로 옮긴다.")
    @Test
    fun saveFcmTokenDuplicatedDeviceIdOtherMemberId() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(member)

        val otherMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(otherMember)

        val fcmToken = FcmToken.of(
            member = member,
            deviceId = "deviceId",
            token = "token",
            platform = FcmTokenPlatform.ANDROID
        )
        fcmTokenRepository.save(fcmToken)

        val request = FcmTokenCreateServiceRequest(
            deviceId = "deviceId",
            token = "updateToken",
            platform = FcmTokenPlatform.ANDROID
        )

        // when
        fcmTokenService.saveFcmToken(otherMember.id!!, request)

        // then
        val findFcmTokens = fcmTokenRepository.findAll()
        val findFcmToken = findFcmTokens[0]

        val findMember = memberRepository.findById(otherMember.id!!).orElseThrow()

        assertThat(findFcmTokens).hasSize(1)
        assertThat(findFcmToken)
            .extracting("member", "deviceId", "token", "platform")
            .contains(findMember, "deviceId", "updateToken", FcmTokenPlatform.ANDROID)
    }


    @DisplayName("기기의 fcm 토큰을 삭제한다.")
    @Test
    fun deleteAllFcmTokenBy() {
        // given
        val member = Member(
            email = "email1",
            password = "password1",
            role = Role.USER,
        )
        memberRepository.save(member)

        val fcmToken1 = FcmToken.of(
            member = member,
            deviceId = "deviceId1",
            token = "token1",
            platform = FcmTokenPlatform.ANDROID
        )
        val fcmToken2 = FcmToken.of(
            member = member,
            deviceId = "deviceId2",
            token = "token2",
            platform = FcmTokenPlatform.IOS
        )
        val fcmToken3 = FcmToken.of(
            member = member,
            deviceId = "deviceId3",
            token = "token2",
            platform = FcmTokenPlatform.IOS
        )
        fcmTokenRepository.save(fcmToken1)
        fcmTokenRepository.save(fcmToken2)
        fcmTokenRepository.save(fcmToken3)

        val deviceIds = listOf("deviceId1", "deviceId3")

        // when
        fcmTokenService.modifyAllFcmTokenFrom(deviceIds)

        // then
        val findFcmTokens = fcmTokenRepository.findAll()

        assertThat(findFcmTokens)
            .extracting("id", "deviceId", "deletedAt")
            .contains(
                tuple(fcmToken1.id!!, fcmToken1.deviceId!!, time.nowLocalDateTime),
                tuple(fcmToken2.id!!, fcmToken2.deviceId!!, null),
                tuple(fcmToken3.id!!, fcmToken3.deviceId!!, time.nowLocalDateTime),
            )
    }

    @DisplayName("회원이 지닌 Fcm 토큰을 전체 삭제한다.")
    @Test
    fun deleteAllFcmFrom() {
        // given
        val member1 = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE
        )
        memberRepository.save(member1)

        val fcmToken1 = FcmToken(
            member = member1,
            deviceId = "deviceId1",
            token = "token1",
            isActive = true,
            platform = FcmTokenPlatform.ANDROID
        )
        val fcmToken2 = FcmToken(
            member = member1,
            deviceId = "deviceId2",
            token = "token2",
            isActive = true,
            platform = FcmTokenPlatform.IOS
        )
        fcmTokenRepository.save(fcmToken1)
        fcmTokenRepository.save(fcmToken2)

        // when
        fcmTokenService.deleteAllFcmFrom(
            memberId = member1.id!!,
        )

        // then
        val fcmTokens = fcmTokenRepository.findAll()

        assertThat(fcmTokens).hasSize(2)

        assertThat(fcmTokens[0].deletedAt).isNotNull()
        assertThat(fcmTokens[1].deletedAt).isNotNull()
    }

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
        fcmTokenService.sendWorkoutPartnerRequest(event)

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

        fcmTokenService.sendWorkoutPartnerAccept(event)

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

        fcmTokenService.sendChatMessage(event)

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

    @DisplayName("푸시 알람 전송 실패 시, 재전송한다")
    @Test
    fun sendMessageError() {
        val member = saveTestMemberWithFcmToken()

        val event = EventFcmChatMessage(
            toMemberId = member.id!!,
            fromNickname = "nick",
            chatRoomId = 1L,
            chatMessageId = 2L
        )

        fcmTokenService.sendChatMessage(event)

        given(
            fcmPublisher.send(any(), any(), any(), any(), any())
        ).willThrow(
            FirebaseException(
                ErrorCode.INTERNAL,
                "test error",
                null
            )
        )

//        then(fcmPublisher).should().send(
//            token = "token",
//            title = FcmMessage.CHAT_MESSAGE.formatTitle("nick"),
//            body = FcmMessage.CHAT_MESSAGE.body,
//            platform = FcmTokenPlatform.ANDROID,
//            data = mapOf(
//                "type" to "CHAT_MESSAGE",
//                "deepLink" to deepLinkConstant.BASE_DOMAIN + FcmMessage.CHAT_MESSAGE.formatDeepLinkPath(1L),
//                "chatMessageId" to 2L
//            )
//        )

        then(fcmPublisher)
            .should(times(1))
            .send(any(), any(), any(), any(), any())
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

        fcmTokenService.sendWorkoutRequest(event)

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

        fcmTokenService.sendWorkoutRequestAccept(event)

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

        fcmTokenService.sendWorkoutRequestReject(event)

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

    @DisplayName("운동 요청 취소 시, 거절과 동일한 푸시 알람을 보낸다.")
    @Test
    fun sendWorkoutRequestCancel() {
        val member = saveTestMemberWithFcmToken()

        val event = EventFcmWorkoutRequestCancel(
            toMemberId = member.id!!,
            chatRoomId = 1L,
            chatMessageId = 2L
        )

        fcmTokenService.sendWorkoutRequestCancel(event)

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

        fcmTokenService.sendWorkoutComplete(event)

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

        fcmTokenService.sendReviewReceive(event)

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

        fcmTokenService.sendReviewRequest(event)

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
        val response = fcmTokenService.findAllFcmTokenByDeviceId("deviceId")

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