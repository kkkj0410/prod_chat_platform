package kr.co.fitview.api.app.domain.member.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.fcm.entity.FcmToken
import kr.co.fitview.api.app.domain.fcm.entity.enums.FcmTokenPlatform
import kr.co.fitview.api.app.domain.fcm.entity.enums.FcmTokenStatus
import kr.co.fitview.api.app.domain.fcm.repository.FcmTokenRepository
import kr.co.fitview.api.app.domain.member.dto.request.MemberWithdrawServiceRequest
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.entity.MemberWithdrawReason
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWithdrawReasonReasonType
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.member.repository.MemberWithdrawReasonRepository
import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.global.entity.OAuth2Provider
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.member.MemberErrorCode
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.ThrowingConsumer
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class MemberWithdrawReasonServiceTest @Autowired constructor(
    val memberRepository: MemberRepository,
    val oAuth2Service : OAuth2Service,
    val memberWithdrawReasonRepository : MemberWithdrawReasonRepository,
    val memberWithdrawReasonService : MemberWithdrawReasonService,
    val fcmTokenRepository : FcmTokenRepository,
    val time : Time
) : IntegrationTestSupport(){

    @DisplayName("회원 계정을 삭제한다.")
    @Test
    fun deleteMember() {
        // given
        val member1 = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE
        )
        memberRepository.save(member1)


        val reason1 = MemberWithdrawReason(
            reasonType = MemberWithdrawReasonReasonType.APP_INCONVENIENCE,
            displayText = MemberWithdrawReasonReasonType.APP_INCONVENIENCE.description,
            seq = 100
        )
        memberWithdrawReasonRepository.save(reason1)

        val request = MemberWithdrawServiceRequest(
            memberWithdrawReasonId = reason1.id!!
        )

        // when
        val deletedMember = memberWithdrawReasonService.deleteMember(
            memberId = member1.id!!,
            request = request
        )


        // then
        assertThat(deletedMember.deletedAt).isNotNull()
        assertThat(deletedMember.memberWithdrawReason).isEqualTo(reason1)
    }

    @DisplayName("회원 계정 삭제 시, 회원이 지닌 Fcm 토큰을 전체 무효화한다.")
    @Test
    fun deleteMemberDeleteFcm() {
        // given
        val member1 = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE
        )
        memberRepository.save(member1)

        val reason1 = MemberWithdrawReason(
            reasonType = MemberWithdrawReasonReasonType.APP_INCONVENIENCE,
            displayText = MemberWithdrawReasonReasonType.APP_INCONVENIENCE.description,
            seq = 100
        )
        memberWithdrawReasonRepository.save(reason1)

        val request = MemberWithdrawServiceRequest(
            memberWithdrawReasonId = reason1.id!!
        )

        val fcmToken1 = FcmToken(
            member = member1,
            deviceId = "deviceId1",
            token = "token1",
            status = FcmTokenStatus.ACTIVE,
            platform = FcmTokenPlatform.ANDROID
        )
        val fcmToken2 = FcmToken(
            member = member1,
            deviceId = "deviceId2",
            token = "token2",
            status = FcmTokenStatus.ACTIVE,
            platform = FcmTokenPlatform.IOS
        )
        fcmTokenRepository.save(fcmToken1)
        fcmTokenRepository.save(fcmToken2)

        // when
        memberWithdrawReasonService.deleteMember(
            memberId = member1.id!!,
            request = request
        )

        // then
        val fcmTokens = fcmTokenRepository.findAll()

        assertThat(fcmTokens).hasSize(2)

        assertThat(fcmTokens[0].status).isEqualTo(FcmTokenStatus.REVOKED)
        assertThat(fcmTokens[1].status).isEqualTo(FcmTokenStatus.REVOKED)
    }


    @DisplayName("회원 계정을 삭제시, 이미 삭제됐으면 다시 삭제할 수 없다.")
    @Test
    fun deleteMemberAlreadyDelete() {
        // given
        val member1 = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE
        )
        memberRepository.save(member1)


        val reason1 = MemberWithdrawReason(
            reasonType = MemberWithdrawReasonReasonType.APP_INCONVENIENCE,
            displayText = MemberWithdrawReasonReasonType.APP_INCONVENIENCE.description,
            seq = 100
        )
        memberWithdrawReasonRepository.save(reason1)

        val request = MemberWithdrawServiceRequest(
            memberWithdrawReasonId = reason1.id!!
        )

        memberWithdrawReasonService.deleteMember(
            memberId = member1.id!!,
            request = request
        )

        // when & then
        assertThatThrownBy {
            memberWithdrawReasonService.deleteMember(
                memberId = member1.id!!,
                request = request
            )
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND)
            })
    }

    @DisplayName("삭제된 회원 계정을 복구한다.")
    @Test
    fun restoreMember() {
        // given
        val member1 = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE
        )
        memberRepository.save(member1)


        val reason1 = MemberWithdrawReason(
            reasonType = MemberWithdrawReasonReasonType.APP_INCONVENIENCE,
            displayText = MemberWithdrawReasonReasonType.APP_INCONVENIENCE.description,
            seq = 100
        )
        memberWithdrawReasonRepository.save(reason1)

        val request = MemberWithdrawServiceRequest(
            memberWithdrawReasonId = reason1.id!!
        )

        memberWithdrawReasonService.deleteMember(
            memberId = member1.id!!,
            request = request
        )

        // when
        val restoredMember = memberWithdrawReasonService.restoreMember(member1.id!!)

        // then
        assertThat(restoredMember.deletedAt).isNull()
        assertThat(restoredMember.memberWithdrawReason).isNull()
    }

    @DisplayName("삭제된 회원 소셜 계정을 복구 시, 이미 동일한 정보로 소셜 계정을 다시 만들었다면 계정 복구 실패한다.")
    @Test
    fun restoreMemberAlreadyOAuth2Member() {
        // given
        val oAuth2Member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE,
            providerId = "123"
        )
        memberRepository.save(oAuth2Member)

        val reason1 = MemberWithdrawReason(
            reasonType = MemberWithdrawReasonReasonType.APP_INCONVENIENCE,
            displayText = MemberWithdrawReasonReasonType.APP_INCONVENIENCE.description,
            seq = 100
        )
        memberWithdrawReasonRepository.save(reason1)

        oAuth2Member.delete(
            now = time.nowLocalDateTime,
            memberWithdrawReason = reason1
        )

        val duplicatedOAuth2Member = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE,
            providerId = "123"
        )
        memberRepository.save(duplicatedOAuth2Member)


        // when & then
        assertThatThrownBy {
            memberWithdrawReasonService.restoreMember(oAuth2Member.id!!)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(MemberErrorCode.MEMBER_DUPLICATE_RECOVER)
            })
    }

    @DisplayName("삭제된 회원 로컬 계정을 복구 시, 이미 동일한 정보로 로컬 계정을 다시 만들었다면 계정 복구 실패한다.")
    @Test
    fun restoreMemberAlreadyLocalMember() {
        // given
        val localMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(localMember)

        val reason1 = MemberWithdrawReason(
            reasonType = MemberWithdrawReasonReasonType.APP_INCONVENIENCE,
            displayText = MemberWithdrawReasonReasonType.APP_INCONVENIENCE.description,
            seq = 100
        )
        memberWithdrawReasonRepository.save(reason1)

        localMember.delete(
            now = time.nowLocalDateTime,
            memberWithdrawReason = reason1
        )

        val duplicatedLocalMember = Member(
            email = "email",
            password = "password",
            role = Role.USER,
        )
        memberRepository.save(duplicatedLocalMember)

        // when & then
        assertThatThrownBy {
            memberWithdrawReasonService.restoreMember(localMember.id!!)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(MemberErrorCode.MEMBER_DUPLICATE_RECOVER)
            })
    }
}