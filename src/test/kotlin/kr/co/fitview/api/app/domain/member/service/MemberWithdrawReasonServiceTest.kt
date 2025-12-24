package kr.co.fitview.api.app.domain.member.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.member.dto.request.MemberWithdrawRequest
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
import kr.co.fitview.api.app.global.util.TestDataFactory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.ThrowingConsumer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class MemberWithdrawReasonServiceTest @Autowired constructor(
    val memberRepository: MemberRepository,
    val oAuth2Service : OAuth2Service,
    val memberWithdrawReasonRepository : MemberWithdrawReasonRepository,
    val memberWithdrawReasonQueryService : MemberWithdrawReasonQueryService,
    val memberWithdrawReasonService : MemberWithdrawReasonService,
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
}