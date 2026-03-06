package kr.co.fitview.api.app.domain.banner.entity

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.banner.entity.enums.BannerDisplayStatus
import kr.co.fitview.api.app.domain.banner.entity.enums.BannerType
import kr.co.fitview.api.app.domain.image.entity.Image
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class BannerTest : IntegrationTestSupport() {


    @DisplayName("배너의 이미지 url을 조회한다.")
    @Test
    fun getImageUrl() {
        // given
        val image = Image(
            url = "url"
        )

        val banner = Banner(
            image = image,
            bannerType = BannerType.APP_FEEDBACK,
            displayStatus = BannerDisplayStatus.ACTIVE
        )

        // when
        val findImageUrl = banner.getImageUrl()

        // then
        assertThat(findImageUrl).isEqualTo(image.url!!)
    }

}
