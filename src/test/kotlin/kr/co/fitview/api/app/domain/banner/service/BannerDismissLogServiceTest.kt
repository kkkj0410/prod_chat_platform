package kr.co.fitview.api.app.domain.banner.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.banner.entity.Banner
import kr.co.fitview.api.app.domain.banner.entity.enums.BannerDisplayStatus
import kr.co.fitview.api.app.domain.banner.entity.enums.BannerType
import kr.co.fitview.api.app.domain.banner.repository.BannerRepository
import kr.co.fitview.api.app.domain.image.entity.Image
import kr.co.fitview.api.app.domain.image.repository.ImageRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class BannerDismissLogServiceTest @Autowired constructor(
    val bannerDismissLogService : BannerDismissLogService,
    val bannerRepository : BannerRepository,
    val imageRepository: ImageRepository,
    val memberRepository: MemberRepository,
    val oAuth2Service : OAuth2Service,
    val time : Time
) : IntegrationTestSupport() {

    @DisplayName("배너를 닫은 기록을 저장한다.")
    @Test
    fun addBannerDismissLog() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(member)

        val image = Image(
            url = "url"
        )
        imageRepository.save(image)

        val banner = Banner(
            image = image,
            bannerType = BannerType.APP_FEEDBACK,
            displayStatus = BannerDisplayStatus.ACTIVE
        )
        bannerRepository.save(banner)

        // when
        val savedBannerDismissLog = bannerDismissLogService.addBannerDismissLog(
            bannerId = banner.id!!,
            memberId = member.id!!
        )

        // then
        assertThat(savedBannerDismissLog.id).isNotNull()
        assertThat(savedBannerDismissLog)
            .extracting(
                "member.id",
                "banner.id",
                "expiresAt"
            )
            .contains(
                member.id,
                banner.id,
                time.nowLocalDateTime.plusDays(1)
            )
    }
}