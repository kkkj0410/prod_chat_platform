package kr.co.fitview.api.app.domain.image.dto.request

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import kr.co.fitview.api.app.domain.image.enums.S3Prefix

data class S3UploadUrlRequest(

    @field:NotNull(message = "prefix is required")
    val prefix : S3Prefix?,

    // 업로드 파일의 바이트 크기 - 50MB(= 52,428,800 bytes) 초과 시, 업로드 불가(기입된 이미지 용량이 실제 파일과 일치하지 않으면 업로드 안됨)
    @field:NotNull(message = "imageByte is required")
    @field:Min(value = 1, message = "imageByte must be at least 1 byte")
    val imageByte : Long?,

    ){
    fun toServiceRequest() : S3UploadUrlServiceRequest {
        return S3UploadUrlServiceRequest(
            prefix = prefix!!,
            imageByte = imageByte!!,
        )
    }

    companion object {
        fun toServiceRequest(requests: List<S3UploadUrlRequest>): List<S3UploadUrlServiceRequest> {
            return requests.map { it.toServiceRequest()}
        }
    }
}
