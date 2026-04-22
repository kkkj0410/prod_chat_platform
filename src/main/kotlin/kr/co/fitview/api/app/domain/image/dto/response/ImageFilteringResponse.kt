package kr.co.fitview.api.app.domain.image.dto.response

import kr.co.fitview.api.app.global.ai.dto.TokenUsage

data class ImageFilteringResponse(
    val message : String,
    val tokenUsage : TokenUsage
)
