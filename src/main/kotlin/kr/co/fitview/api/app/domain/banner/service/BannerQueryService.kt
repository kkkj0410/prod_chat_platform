package kr.co.fitview.api.app.domain.banner.service

import kr.co.fitview.api.app.domain.banner.dto.response.BannerActiveResponse
import kr.co.fitview.api.app.domain.banner.entity.Banner
import kr.co.fitview.api.app.domain.banner.entity.enums.BannerType
import kr.co.fitview.api.app.domain.banner.repository.BannerRepository
import kr.co.fitview.api.app.domain.workout_reward.service.WorkoutRewardQueryService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class BannerQueryService(
    private val bannerRepository: BannerRepository,
    private val workoutRewardQueryService: WorkoutRewardQueryService,

    @Value("\${banner.app-feedback.svg-image-url}")
    private val bannerAppFeedbackSvgImageUrl: String,
) {

    fun findActiveBanners(memberId : Long) : List<BannerActiveResponse> {

        val responses = mutableListOf<BannerActiveResponse>()

        bannerRepository.findActiveBannerByType(memberId, BannerType.APP_FEEDBACK)?.let {
            responses.add(
                BannerActiveResponse.AppFeedback(
                    bannerId = it.id!!,
                    type = it.bannerType!!,
                    imageUrl = it.getImageUrl(),
                    svgImageUrl = bannerAppFeedbackSvgImageUrl
                )
            )
        }

        bannerRepository.findActiveBannerByType(memberId, BannerType.WORKOUT_REWARD)?.let {
            if (isDisplayWorkoutRewardBanner(memberId)) {
                responses.add(
                    BannerActiveResponse.General(
                        bannerId = it.id!!,
                        type = it.bannerType!!,
                    )
                )
            }
        }

        return responses
    }


    fun findBannerReferenceFrom(bannerId : Long) : Banner {
        return bannerRepository.getReferenceById(bannerId)
    }

    private fun isDisplayWorkoutRewardBanner(memberId: Long): Boolean {
        val stampCount = workoutRewardQueryService.findWorkoutRewardStamp(memberId).stampCount
        return stampCount < 1
    }
}