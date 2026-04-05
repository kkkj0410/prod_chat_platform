package kr.co.fitview.api.app.domain.banner.service

import kr.co.fitview.api.app.domain.banner.dto.response.BannerActiveResponse
import kr.co.fitview.api.app.domain.banner.entity.Banner
import kr.co.fitview.api.app.domain.banner.entity.enums.BannerType
import kr.co.fitview.api.app.domain.banner.repository.BannerRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import software.amazon.awssdk.core.internal.waiters.ResponseOrException.response
import kotlin.random.Random

@Service
@Transactional(readOnly = true)
class BannerQueryService(
    private val bannerRepository: BannerRepository,

    @Value("\${banner.app-feedback.svg-image-url}")
    private val bannerAppFeedbackSvgImageUrl: String,
) {

    fun findActiveBanners(memberId : Long) : List<BannerActiveResponse> {

        val findBanners = bannerRepository.findAllActiveBanner(memberId)

        val responses = findBanners.map {
            when (it.bannerType) {
                BannerType.APP_FEEDBACK -> BannerActiveResponse.AppFeedback(
                    bannerId = it.id!!,
                    type = it.bannerType!!,
                    imageUrl = it.getImageUrl(),
                    svgImageUrl = bannerAppFeedbackSvgImageUrl
                )
                else -> BannerActiveResponse.General(
                    bannerId = it.id!!,
                    type = it.bannerType!!,
                )
            }
        }.toMutableList()

        if (Random.nextBoolean()) {
            responses.add(
                BannerActiveResponse.General(
                    bannerId = 2L,
                    type = BannerType.WORKOUT_REWARD
                )
            )
        }

        return responses

//        return findBanners.map {
//            when (it.bannerType) {
//                BannerType.APP_FEEDBACK -> BannerActiveResponse.AppFeedback(
//                    bannerId = it.id!!,
//                    type = it.bannerType!!,
//                    imageUrl = it.getImageUrl(),
//                    svgImageUrl = bannerAppFeedbackSvgImageUrl
//                )
//                else -> BannerActiveResponse.General(
//                    bannerId = it.id!!,
//                    type = it.bannerType!!,
//                )
//            }
//        }

    }

    fun findBannerReferenceFrom(bannerId : Long) : Banner {
        return bannerRepository.getReferenceById(bannerId)
    }
}