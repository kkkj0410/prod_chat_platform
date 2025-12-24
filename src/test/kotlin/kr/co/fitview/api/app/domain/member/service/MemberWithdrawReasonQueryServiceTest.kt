package kr.co.fitview.api.app.domain.member.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.member.condition.AdminWithdrawMemberCondition
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.entity.MemberWithdrawReason
import kr.co.fitview.api.app.domain.member.entity.enums.MemberWithdrawReasonReasonType
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.member.repository.MemberWithdrawReasonRepository
import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.global.entity.OAuth2Provider
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import kr.co.fitview.api.app.global.time.TimeHolder.time
import kr.co.fitview.api.app.global.util.TestDataFactory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class MemberWithdrawReasonQueryServiceTest @Autowired constructor(
    val memberRepository: MemberRepository,
    val oAuth2Service : OAuth2Service,
    val memberWithdrawReasonRepository : MemberWithdrawReasonRepository,
    val memberWithdrawReasonQueryService : MemberWithdrawReasonQueryService,
    val time : Time
) : IntegrationTestSupport(){


    @DisplayName("탈퇴한 회원을 전체 조회한다.")
    @Test
    fun findAllMemberWithdrawReasonBy() {
        // given
        val member1 = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE
        )
        val member2 = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE
        )
        val member3 = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            provider = OAuth2Provider.APPLE
        )
        memberRepository.save(member1)
        memberRepository.save(member2)
        memberRepository.save(member3)

        val request1 = TestDataFactory.oAuth2SignupRequest(
            nickname = "member1"
        )
        oAuth2Service.signup(request1, member1.id!!)

        val request2 = TestDataFactory.oAuth2SignupRequest(
            nickname = "member2"
        )
        oAuth2Service.signup(request2, member2.id!!)

        val request3 = TestDataFactory.oAuth2SignupRequest(
            nickname = "member3"
        )
        oAuth2Service.signup(request3, member3.id!!)


        val reason1 = MemberWithdrawReason(
            reasonType = MemberWithdrawReasonReasonType.APP_INCONVENIENCE,
            displayText = MemberWithdrawReasonReasonType.APP_INCONVENIENCE.description,
            seq = 100
        )
        val reason2 = MemberWithdrawReason(
            reasonType = MemberWithdrawReasonReasonType.FOUND_MATE,
            displayText = MemberWithdrawReasonReasonType.FOUND_MATE.description,
            seq = 200
        )
        val reason3 = MemberWithdrawReason(
            reasonType = MemberWithdrawReasonReasonType.NO_DESIRED_MATCH,
            displayText = MemberWithdrawReasonReasonType.NO_DESIRED_MATCH.description,
            seq = 300
        )
        memberWithdrawReasonRepository.save(reason1)
        memberWithdrawReasonRepository.save(reason2)
        memberWithdrawReasonRepository.save(reason3)

        member1.delete(
            now = time.nowLocalDateTime,
            memberWithdrawReason = reason1
        )
        member2.delete(
            now = time.nowLocalDateTime,
            memberWithdrawReason = reason2
        )
        member3.delete(
            now = time.nowLocalDateTime,
            memberWithdrawReason = reason3
        )

        val condition = AdminWithdrawMemberCondition()

        // when
        val response = memberWithdrawReasonQueryService.findAllMemberWithdrawReasonFrom(condition)

        // then
        assertThat(response)
            .extracting("memberId", "nickname", "deletedAt", "memberWithdrawReasonDisplayText")
            .containsExactly(
                tuple(
                    member3.id!!,
                    request3.nickname,
                    member3.deletedAt,
                    MemberWithdrawReasonReasonType.NO_DESIRED_MATCH.description
                ),
                tuple(
                    member2.id!!,
                    request2.nickname,
                    member2.deletedAt,
                    MemberWithdrawReasonReasonType.FOUND_MATE.description
                ),
                tuple(
                    member1.id!!,
                    request1.nickname,
                    member1.deletedAt,
                    MemberWithdrawReasonReasonType.APP_INCONVENIENCE.description
                ),
            )
    }
}