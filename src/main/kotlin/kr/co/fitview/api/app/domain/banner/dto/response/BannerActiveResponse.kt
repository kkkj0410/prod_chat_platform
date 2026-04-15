package kr.co.fitview.api.app.domain.banner.dto.response

import kr.co.fitview.api.app.domain.banner.entity.enums.BannerType

sealed class BannerActiveResponse {
    abstract val bannerId: Long
    abstract val type: BannerType
    // image 넣을 시, SVG 파일로 해야함

    data class General(
        override val bannerId: Long,
        override val type: BannerType,
    ) : BannerActiveResponse()

    data class AppFeedback(
        override val bannerId: Long,
        override val type: BannerType,
        // PNG 파일로 이미 들어간 상태(2026.4.1). 따라서 별도로 svgImageUrl을 만듦
        val imageUrl : String,
        // SVG 파일
        val svgImageUrl: String,
    ) : BannerActiveResponse()
}