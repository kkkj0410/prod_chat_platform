package kr.co.fitview.api.app.domain.banner.repository

import kr.co.fitview.api.app.domain.banner.entity.BannerDismissLog
import org.springframework.data.jpa.repository.JpaRepository

interface BannerDismissLogRepository : JpaRepository<BannerDismissLog, Long> {
}