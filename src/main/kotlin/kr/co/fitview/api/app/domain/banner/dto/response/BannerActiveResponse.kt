package kr.co.fitview.api.app.domain.banner.dto.response

import kr.co.fitview.api.app.domain.banner.entity.enums.BannerType

data class BannerActiveResponse(
    val bannerId : Long,
    val type : BannerType,
    val imageUrl : String
)