package kr.co.fitview.api.app.domain.banner.service

import kr.co.fitview.api.app.domain.banner.dto.response.BannerActiveResponse
import kr.co.fitview.api.app.domain.banner.entity.Banner
import kr.co.fitview.api.app.domain.banner.entity.enums.BannerType
import kr.co.fitview.api.app.domain.banner.repository.BannerRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import software.amazon.awssdk.core.internal.waiters.ResponseOrException.response
import kotlin.random.Random

@Service
@Transactional(readOnly = true)
class BannerQueryService(
    private val bannerRepository: BannerRepository,

) {

    fun findActiveBanners(memberId : Long) : List<BannerActiveResponse> {

        val findBanners = bannerRepository.findAllActiveBanner(memberId)

        return findBanners.map{
            BannerActiveResponse(
                bannerId = it.id!!,
                type = it.bannerType!!,
                imageUrl = it.getImageUrl()
            )
        }

    }

    fun findBannerReferenceFrom(bannerId : Long) : Banner {
        return bannerRepository.getReferenceById(bannerId)
    }
}