package kr.co.fitview.api.app.domain.image.dto.request

import jakarta.validation.constraints.NotBlank

data class ImageFilteringRequest(
    @field:NotBlank(message = "imageUrl is required")
    val imageUrl: String
)
