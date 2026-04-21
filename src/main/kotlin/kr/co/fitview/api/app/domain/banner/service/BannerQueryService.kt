package kr.co.fitview.api.app.domain.banner.service

import kr.co.fitview.api.app.domain.banner.dto.response.BannerActiveResponse
import kr.co.fitview.api.app.domain.banner.entity.Banner
import kr.co.fitview.api.app.domain.banner.entity.enums.BannerType
import kr.co.fitview.api.app.domain.banner.repository.BannerRepository
import kr.co.fitview.api.app.domain.workout_reward.service.WorkoutRewardQueryService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import software.amazon.awssdk.core.internal.waiters.ResponseOrException.response
import kotlin.random.Random

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

        // 1. 앱 피드백 배너 조회
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

        // 2. 운동 리워드 배너 조회
        bannerRepository.findActiveBannerByType(memberId, BannerType.WORKOUT_REWARD)?.let {
            val stampCount = workoutRewardQueryService.findWorkoutRewardStamp(memberId).stampCount
            // 스탬프 현황이 1개 이상이면 조회되지 않음
            if (stampCount < 1) {
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
}