package kr.co.fitview.api.app.domain.image.controller

import jakarta.validation.Valid
import kr.co.fitview.api.app.domain.image.dto.request.S3UploadUrlRequest
import kr.co.fitview.api.app.domain.image.dto.response.S3UploadUrlResponse
import kr.co.fitview.api.app.domain.image.service.S3Service
import kr.co.fitview.api.app.global.dto.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/v1/images")
class ImageController(
    private val s3Service: S3Service
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
}