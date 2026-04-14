package kr.co.fitview.api.app.domain.banner.repository

import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.fitview.api.app.domain.banner.entity.Banner
import kr.co.fitview.api.app.domain.banner.entity.QBanner
import kr.co.fitview.api.app.domain.banner.entity.QBanner.banner
import kr.co.fitview.api.app.domain.banner.entity.QBannerDismissLog.bannerDismissLog
import kr.co.fitview.api.app.domain.banner.entity.enums.BannerDisplayStatus
import kr.co.fitview.api.app.domain.image.entity.QImage.image
import kr.co.fitview.api.app.global.time.Time

class BannerRepositoryImpl(
    private val queryFactory : JPAQueryFactory,
    private val time : Time
) : BannerRepositoryCustom {


    override fun findAllActiveBanner(memberId : Long): List<Banner> {
        val subBanner = QBanner("subBanner")
        val twentyFourHoursAgo = time.nowLocalDateTime.minusHours(24)

        return queryFactory
            .selectFrom(banner)
            .leftJoin(banner.image, image).fetchJoin()
            .leftJoin(bannerDismissLog).on(
                bannerDismissLog.banner.id.eq(banner.id),
                bannerDismissLog.member.id.eq(memberId)
            )
            .where(
                banner.displayStatus.eq(BannerDisplayStatus.ACTIVE),

                JPAExpressions
                    .selectOne()
                    .from(subBanner)
                    .where(
                        subBanner.bannerType.eq(banner.bannerType),
                        subBanner.displayStatus.eq(BannerDisplayStatus.ACTIVE),
                        subBanner.createdAt.gt(banner.createdAt)
                    ).notExists(),

                bannerDismissLog.id.isNull.or(
                    bannerDismissLog.expiresAt.loe(twentyFourHoursAgo)
                )
            )
            .distinct() // 혹시 모를 중복 ROW 방지
            .fetch()

    }
}