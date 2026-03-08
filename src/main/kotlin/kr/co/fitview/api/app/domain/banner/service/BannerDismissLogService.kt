package kr.co.fitview.api.app.domain.banner.service

import kr.co.fitview.api.app.domain.banner.entity.BannerDismissLog
import kr.co.fitview.api.app.domain.banner.repository.BannerDismissLogRepository
import kr.co.fitview.api.app.domain.banner.repository.BannerRepository
import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.global.time.Time
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional
class BannerDismissLogService(
    private val memberQueryService : MemberQueryService,
    private val bannerQueryService : BannerQueryService,
    private val bannerDismissLogRepository : BannerDismissLogRepository,
    private val time : Time
) {

    fun addBannerDismissLog(bannerId : Long, memberId: Long) : BannerDismissLog {

        val findMember = memberQueryService.findMemberReferenceFrom(memberId)
        val findBanner = bannerQueryService.findBannerReferenceFrom(bannerId)

        val bannerDismissLog = BannerDismissLog(
            member = findMember,
            banner = findBanner,
            expiresAt = time.nowLocalDateTime.plusDays(1)
        )

        return bannerDismissLogRepository.save(bannerDismissLog)
    }


}