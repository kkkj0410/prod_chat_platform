package kr.co.fitview.api.app.domain.banner.repository

import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.fitview.api.app.domain.banner.entity.Banner
import kr.co.fitview.api.app.domain.banner.entity.QBanner
import kr.co.fitview.api.app.domain.banner.entity.QBanner.banner
import kr.co.fitview.api.app.domain.banner.entity.QBannerDismissLog.bannerDismissLog
import kr.co.fitview.api.app.domain.banner.entity.enums.BannerDisplayStatus
import kr.co.fitview.api.app.domain.banner.entity.enums.BannerType
import kr.co.fitview.api.app.domain.image.entity.QImage.image
import kr.co.fitview.api.app.global.time.Time

class BannerRepositoryImpl(
    private val queryFactory : JPAQueryFactory,
    private val time : Time
) : BannerRepositoryCustom {


    override fun findActiveBannerByType(memberId: Long, bannerType: BannerType): Banner? {
        val now = time.nowLocalDateTime

        return queryFactory
            .selectFrom(banner)
            .leftJoin(banner.image, image).fetchJoin()
            .where(
                banner.bannerType.eq(bannerType),
                banner.displayStatus.eq(BannerDisplayStatus.ACTIVE),

                JPAExpressions
                    .selectOne()
                    .from(bannerDismissLog)
                    .where(
                        bannerDismissLog.banner.id.eq(banner.id),
                        bannerDismissLog.member.id.eq(memberId),
                        bannerDismissLog.expiresAt.gt(now)
                    ).notExists()
            )
            .distinct()
            .fetchOne()
    }
}