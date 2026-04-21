package kr.co.fitview.api.app.domain.banner.repository

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.banner.entity.Banner
import kr.co.fitview.api.app.domain.banner.entity.BannerDismissLog
import kr.co.fitview.api.app.domain.banner.entity.enums.BannerDisplayStatus
import kr.co.fitview.api.app.domain.banner.entity.enums.BannerType
import kr.co.fitview.api.app.domain.image.entity.Image
import kr.co.fitview.api.app.domain.image.repository.ImageRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
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

    @DisplayName("회원 ID와 배너 유형으로 활성화된 배너를 조회한다.")
    @Test
    fun findActiveBannerByType() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(member)

        val image = Image(url = "url")
        imageRepository.save(image)

        val banner = Banner(
            image = image,
            bannerType = BannerType.APP_FEEDBACK,
            displayStatus = BannerDisplayStatus.ACTIVE
        )
        bannerRepository.save(banner)

        // when
        val findBanner = bannerRepository.findActiveBannerByType(member.id!!, BannerType.APP_FEEDBACK)

        // then
        assertThat(findBanner).isNotNull
        assertThat(findBanner!!.id).isEqualTo(banner.id)
        assertThat(findBanner.bannerType).isEqualTo(BannerType.APP_FEEDBACK)
    }

    @DisplayName("배너 상태가 활성화(ACTIVE)가 아니면 조회되지 않는다.")
    @Test
    fun findActiveBannerByTypeInactiveStatus() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(member)

        val banner = Banner(
            image = null,
            bannerType = BannerType.APP_FEEDBACK,
            displayStatus = BannerDisplayStatus.INACTIVE
        )
        bannerRepository.save(banner)

        // when
        val findBanner = bannerRepository.findActiveBannerByType(member.id!!, BannerType.APP_FEEDBACK)

        // then
        assertThat(findBanner).isNull()
    }

    @DisplayName("요청한 배너 유형과 일치하는 배너가 없으면 조회되지 않는다.")
    @Test
    fun findActiveBannerByTypeDifferentType() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(member)

        val banner = Banner(
            image = null,
            bannerType = BannerType.APP_FEEDBACK,
            displayStatus = BannerDisplayStatus.ACTIVE
        )
        bannerRepository.save(banner)

        // when
        val findBanner = bannerRepository.findActiveBannerByType(member.id!!, BannerType.WORKOUT_REWARD)

        // then
        assertThat(findBanner).isNull()
    }

    @DisplayName("만료되지 않은 배너 숨김 로그가 존재하면 조회되지 않는다.")
    @Test
    fun findActiveBannerByTypeExistsDismissLog() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(member)

        val banner = Banner(
            image = null,
            bannerType = BannerType.APP_FEEDBACK,
            displayStatus = BannerDisplayStatus.ACTIVE
        )
        bannerRepository.save(banner)

        val bannerDismissLog = BannerDismissLog(
            member = member,
            banner = banner,
            expiresAt = time.nowLocalDateTime.plusHours(1)
        )
        bannerDismissLogRepository.save(bannerDismissLog)

        // when
        val findBanner = bannerRepository.findActiveBannerByType(member.id!!, BannerType.APP_FEEDBACK)

        // then
        assertThat(findBanner).isNull()
    }

    @DisplayName("배너 숨김 로그가 만료되었으면 조회된다.")
    @Test
    fun findActiveBannerByTypeExistsExpiredDismissLog() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(member)

        val banner = Banner(
            image = null,
            bannerType = BannerType.APP_FEEDBACK,
            displayStatus = BannerDisplayStatus.ACTIVE
        )
        bannerRepository.save(banner)

        val bannerDismissLog = BannerDismissLog(
            member = member,
            banner = banner,
            expiresAt = time.nowLocalDateTime.minusSeconds(1)
        )
        bannerDismissLogRepository.save(bannerDismissLog)

        // when
        val findBanner = bannerRepository.findActiveBannerByType(member.id!!, BannerType.APP_FEEDBACK)

        // then
        assertThat(findBanner).isNotNull
        assertThat(findBanner!!.id).isEqualTo(banner.id)
    }
}