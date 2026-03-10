package kr.co.fitview.api.app.domain.banner.repository

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.banner.entity.Banner
import kr.co.fitview.api.app.domain.banner.entity.BannerDismissLog
import kr.co.fitview.api.app.domain.banner.entity.QBanner.banner
import kr.co.fitview.api.app.domain.banner.entity.enums.BannerDisplayStatus
import kr.co.fitview.api.app.domain.banner.entity.enums.BannerType
import kr.co.fitview.api.app.domain.banner.service.BannerDismissLogService
import kr.co.fitview.api.app.domain.image.entity.Image
import kr.co.fitview.api.app.domain.image.entity.QImage.image
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

class BannerRepositoryTest @Autowired constructor(
    val bannerRepository : BannerRepository,
    val imageRepository: ImageRepository,
    val memberRepository: MemberRepository,
    val oAuth2Service : OAuth2Service,
    val bannerDismissLogRepository : BannerDismissLogRepository,
    val time : Time
) : IntegrationTestSupport() {

    @DisplayName("해당 회원에게 활성화된 모든 배너를 조회한다.")
    @Test
    fun findAllActiveBanner() {
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

        val image2 = Image(
            url = "url"
        )
        imageRepository.save(image2)

        val banner2 = Banner(
            image = image2,
            bannerType = BannerType.ETC,
            displayStatus = BannerDisplayStatus.ACTIVE
        )
        bannerRepository.save(banner2)


        // when
        val findBanners = bannerRepository.findAllActiveBanner(member.id!!)

        // then
        assertThat(findBanners).hasSize(2)
        assertThat(findBanners)
            .extracting("id")
            .contains(
                banner1.id!!, banner2.id!!
            )
    }

    @DisplayName("활성화된 배너 전체 조회 시, 중복 타입의 배너는 최근 1개만 조회한다.")
    @Test
    fun findAllActiveBannerDuplicatedType() {
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
        banner1.createdAt = time.nowLocalDateTime.minusSeconds(1)
        bannerRepository.save(banner1)

        val image2 = Image(
            url = "url"
        )
        imageRepository.save(image2)

        val banner2 = Banner(
            image = image2,
            bannerType = BannerType.APP_FEEDBACK,
            displayStatus = BannerDisplayStatus.ACTIVE
        )
        banner2.createdAt = time.nowLocalDateTime
        bannerRepository.save(banner2)


        // when
        val findBanners = bannerRepository.findAllActiveBanner(member.id!!)

        // then
        assertThat(findBanners).hasSize(1)
        assertThat(findBanners)
            .extracting("id")
            .contains(
                banner2.id!!
            )
    }

    @DisplayName("해당 회원에게 활성화된 배너 전체 조회 시, 활성화된 배너만 보인다.")
    @Test
    fun findAllActiveBannerOnlyActiveType() {
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

        val image2 = Image(
            url = "url"
        )
        imageRepository.save(image2)

        val banner2 = Banner(
            image = image2,
            bannerType = BannerType.ETC,
            displayStatus = BannerDisplayStatus.INACTIVE
        )
        bannerRepository.save(banner2)


        // when
        val findBanners = bannerRepository.findAllActiveBanner(member.id!!)

        // then
        assertThat(findBanners).hasSize(1)
        assertThat(findBanners[0].id).isEqualTo(banner1.id!!)
    }

    @DisplayName("활성화 배너 전체 조회 시, 배너 닫기 기록이 24시간 내에 있으면 해당 배너는 조회하지 않는다.")
    @Test
    fun findAllActiveBannerExistsDismissLog() {
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

        val image2 = Image(
            url = "url"
        )
        imageRepository.save(image2)

        val banner2 = Banner(
            image = image2,
            bannerType = BannerType.ETC,
            displayStatus = BannerDisplayStatus.ACTIVE
        )
        bannerRepository.save(banner2)

        val bannerDismissLog = BannerDismissLog(
            member = member,
            banner = banner2,
            expiresAt = time.nowLocalDateTime.minusHours(23).minusMinutes(59).minusSeconds(59)
        )
        bannerDismissLogRepository.save(bannerDismissLog)


        // when
        val findBanners = bannerRepository.findAllActiveBanner(member.id!!)

        // then
        assertThat(findBanners).hasSize(1)
        assertThat(findBanners)
            .extracting("id")
            .contains(
                banner1.id!!
            )
    }

    @DisplayName("활성화 배너 전체 조회 시, 배너 닫기 기록이 24시간이 지났으면 해당 배너는 조회한다.")
    @Test
    fun findAllActiveBannerExistsExpiredDismissLog() {
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

        val image2 = Image(
            url = "url"
        )
        imageRepository.save(image2)

        val banner2 = Banner(
            image = image2,
            bannerType = BannerType.ETC,
            displayStatus = BannerDisplayStatus.ACTIVE
        )
        bannerRepository.save(banner2)

        val bannerDismissLog = BannerDismissLog(
            member = member,
            banner = banner2,
            expiresAt = time.nowLocalDateTime.minusHours(24)
        )
        bannerDismissLogRepository.save(bannerDismissLog)


        // when
        val findBanners = bannerRepository.findAllActiveBanner(member.id!!)

        // then
        assertThat(findBanners).hasSize(2)
        assertThat(findBanners)
            .extracting("id")
            .contains(
                banner1.id!!, banner2.id!!
            )
    }
}