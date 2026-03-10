package kr.co.fitview.api.app.domain.banner.repository

import kr.co.fitview.api.app.domain.banner.entity.Banner

interface BannerRepositoryCustom {

    fun findAllActiveBanner(memberId : Long) : List<Banner>
}