package kr.co.fitview.api.app.domain.image.controller

import jakarta.validation.Valid
import kr.co.fitview.api.app.domain.image.dto.request.ImageFilteringRequest
import kr.co.fitview.api.app.domain.image.dto.request.S3UploadUrlRequest
import kr.co.fitview.api.app.domain.image.dto.response.ImageFilteringFinalResponse
import kr.co.fitview.api.app.domain.image.dto.response.ImageFilteringResponse
import kr.co.fitview.api.app.domain.image.dto.response.S3UploadUrlResponse
import kr.co.fitview.api.app.domain.image.service.ImageAiService
import kr.co.fitview.api.app.domain.image.service.S3Service
import kr.co.fitview.api.app.global.ai.dto.TokenUsage
import kr.co.fitview.api.app.global.ai.service.ImageModerationService.ModerationAnalysis
import kr.co.fitview.api.app.global.dto.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/v1/images")
class ImageController(
    private val s3Service: S3Service,
    private val imageAiService: ImageAiService
) {

    @PostMapping("/presign")
    fun presign(

        @Valid
        @RequestBody
        request : List<S3UploadUrlRequest>

    ): ResponseEntity<ApiResponse<List<S3UploadUrlResponse>>>
    {
        val response = s3Service.getAllUploadUrls(S3UploadUrlRequest.toServiceRequest(request))

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @PostMapping("/filter")
    fun filter(
        @Valid
        @RequestBody
        request: ImageFilteringRequest
    ): ResponseEntity<ApiResponse<ImageFilteringFinalResponse>> {

        val response = imageAiService.filterImage1(request.imageUrl)

        if(response.isAdult){
            val return1 =  ImageFilteringFinalResponse(
                isAdult = true,
                message = "음란물입니다.",
                tokenUsage = TokenUsage(
                    promptTokens = 0,
                    completionTokens = 0,
                    totalTokens = 0
                )
            )
            return ResponseEntity.ok(ApiResponse.success(return1))
        }


        val response1 = imageAiService.filterImage2(request.imageUrl)

        val response2 = ImageFilteringFinalResponse(
            isAdult = false,
            message = response1.message,
            tokenUsage = response1.tokenUsage
        )

        return ResponseEntity.ok(ApiResponse.success(response2))
    }
}