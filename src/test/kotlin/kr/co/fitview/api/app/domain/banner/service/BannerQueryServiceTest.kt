package kr.co.fitview.api.app.domain.banner.service

import jakarta.persistence.EntityManager
import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.auth.repository.RefreshTokenRepository
import kr.co.fitview.api.app.domain.auth.service.RefreshTokenQueryService
import kr.co.fitview.api.app.domain.banner.dto.response.BannerActiveResponse
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
import org.assertj.core.api.Assertions.tuple
import org.hibernate.proxy.HibernateProxy
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class BannerQueryServiceTest @Autowired constructor(
    val bannerQueryService : BannerQueryService,
    val bannerRepository : BannerRepository,
    val imageRepository: ImageRepository,
    val memberRepository: MemberRepository,
    val oAuth2Service : OAuth2Service,
    val time : Time,
    val em : EntityManager
) : IntegrationTestSupport() {

    @DisplayName("배너를 프록시로 조회한다.")
    @Test
    fun findBannerReferenceFrom() {
        // given
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

        em.flush()
        em.clear()

        // when
        val findBanner = bannerQueryService.findBannerReferenceFrom(banner.id!!)


        // then
        assertThat(findBanner).isInstanceOf(HibernateProxy::class.java)
        assertThat(findBanner::class.simpleName!!).contains("Banner")
        assertThat(findBanner)
            .extracting("image.url", "bannerType")
            .contains(
                image.url,
                BannerType.APP_FEEDBACK
            )
    }

    @DisplayName("해당 회원에게 활성화된 모든 배너를 조회한다.")
    @Test
    fun findActiveBanners() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(member)

        val image1 = Image(
            url = "url"
        )
        imageRepository.save(image1)

        val banner1 = Banner(
            image = image1,
            bannerType = BannerType.APP_FEEDBACK,
            displayStatus = BannerDisplayStatus.ACTIVE
        )
        bannerRepository.save(banner1)

        val banner2 = Banner(
            image = null,
            bannerType = BannerType.WORKOUT_REWARD,
            displayStatus = BannerDisplayStatus.ACTIVE
        )
        bannerRepository.save(banner2)

        // when
        val findBanners = bannerQueryService.findActiveBanners(member.id!!)

        // then
        assertThat(findBanners).hasSize(2)

        val resultBanner1 = findBanners.find { it.bannerId == banner1.id } as BannerActiveResponse.AppFeedback
        assertThat(resultBanner1.type).isEqualTo(banner1.bannerType)
        assertThat(resultBanner1.imageUrl).isEqualTo(image1.url)

        val resultBanner2 = findBanners.find { it.bannerId == banner2.id } as BannerActiveResponse.General
        assertThat(resultBanner2.type).isEqualTo(banner2.bannerType)
    }
}