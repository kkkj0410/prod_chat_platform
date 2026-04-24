package kr.co.fitview.api.app.domain.banner.repository

import kr.co.fitview.api.app.domain.banner.entity.Banner
import kr.co.fitview.api.app.domain.banner.entity.enums.BannerType

interface BannerRepositoryCustom {

    fun findActiveBannerByType(memberId: Long, bannerType: BannerType): Banner?
}