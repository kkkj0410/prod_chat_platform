package kr.co.fitview.api.app.domain.member.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.address.repository.AddressRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.util.TestDataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class MemberQueryServiceTest @Autowired constructor(
    val memberQueryService: MemberQueryService,
    val memberRepository : MemberRepository,
    val oAuth2Service : OAuth2Service
) : IntegrationTestSupport(){

    @DisplayName("운동 파트너 요청에서 요청자의 프로필을 조회한다.")
    @Test
    fun findMemberWorkoutRequestProfileFrom() {
        // given
        val me = Member(
            email = "email1",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(me)

        val request = TestDataFactory.oAuth2SignupRequest()
        oAuth2Service.signup(request, me.id!!)

        // when
        val findMemberProfile = memberQueryService.findMemberWorkoutRequestProfileFrom(me.id!!)

        // then
        assertThat(findMemberProfile)
            .extracting("memberId", "profileImageUrl", "nickname")
            .contains(me.id!!, request.profileImageUrl, request.nickname)
    }
}