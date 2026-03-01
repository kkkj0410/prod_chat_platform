package kr.co.fitview.api.app.domain.banner.service

import kr.co.fitview.api.app.domain.banner.dto.response.BannerActiveResponse
import kr.co.fitview.api.app.domain.banner.entity.enums.BannerType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.random.Random

@Service
@Transactional(readOnly = true)
class BannerQueryService {


    fun findActiveBanners() : List<BannerActiveResponse> {

        val isListEmpty = Random.nextBoolean()

        val response = if (isListEmpty) {
            emptyList()
        } else {
            listOf(
                BannerActiveResponse(
                    bannerId = 1L,
                    type = BannerType.APP_FEEDBACK,
                    imageUrl = "https://static-dev.fitview.co.kr/app-feedback/banner/68ed0a37-b2a4-4792-adc9-cb673e08cf99"
                )
            )
        }

        return response
    }
}