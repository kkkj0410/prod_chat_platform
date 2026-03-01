package kr.co.fitview.api.app.domain.banner.repository

import kr.co.fitview.api.app.domain.banner.entity.Banner
import org.springframework.data.jpa.repository.JpaRepository

interface BannerRepository : JpaRepository<Banner, Long> {
}