package kr.co.fitview.api.app.domain.image.service

import kr.co.fitview.api.app.domain.image.config.S3Config
import kr.co.fitview.api.app.domain.image.dto.request.S3UploadUrlServiceRequest
import kr.co.fitview.api.app.domain.image.dto.response.S3UploadUrlResponse
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.image.ImageErrorCode
import kr.co.fitview.api.app.global.id.IdGenerator
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.net.URL
import java.time.Duration


@Service
class S3Service(
    private val s3Config: S3Config,
    private val s3Presigner: S3Presigner,
    private val s3Client: S3Client,
    private val idGenerator: IdGenerator
) {

    fun getAllUploadUrls(requests : List<S3UploadUrlServiceRequest>): List<S3UploadUrlResponse> {
        if(isImageCountExceeded(requests)){
            throw GlobalException(ImageErrorCode.S3_UPLOAD_COUNT_LIMIT)
        }

        return requests.map { createUploadUrl(it) }
    }

    fun downloadImage(imageUrl: String): Resource {
        val key = extractKey(imageUrl)
        val getObjectRequest = GetObjectRequest.builder()
            .bucket(s3Config.bucket)
            .key(key)
            .build()

        val response = s3Client.getObjectAsBytes(getObjectRequest)
        return ByteArrayResource(response.asByteArray())
    }

    private fun extractKey(imageUrl: String): String {
        val domainPrefix = "https://${s3Config.domain}/"
        return if (imageUrl.startsWith(domainPrefix)) {
            imageUrl.removePrefix(domainPrefix)
        } else {
            // Handle other URL formats if necessary, or throw exception
            imageUrl.substringAfter("${s3Config.domain}/")
        }
    }

    private fun isImageCountExceeded(request: List<S3UploadUrlServiceRequest>) =
        request.size > s3Config.maxImageCount

    private fun createUploadUrl(
        request : S3UploadUrlServiceRequest
    ): S3UploadUrlResponse {

        val fullImageName = createFullImageName(request)

        val presignedUrl = createPresignedUrl(fullImageName, request.imageByte)

        val accessUrl = getAccessUrl(fullImageName)

        return S3UploadUrlResponse(presignedUrl, accessUrl)
    }

    private fun createFullImageName(request: S3UploadUrlServiceRequest) =
        request.prefix.value + "/" + idGenerator.createUuid()

    private fun createPresignedUrl(
        fullFileName: String,
        imageByte: Long,
    ): String {
        if (isImageTooLarge(imageByte)) {
            throw GlobalException(ImageErrorCode.S3_IMAGE_TOO_LARGE)
        }

        val putObjectRequest = getPutObjectRequest(fullFileName, imageByte)

        val putObjectPresignRequest = getPutObjectPresignRequest(putObjectRequest)

        return presignedUrl(putObjectPresignRequest)
    }

    private fun isImageTooLarge(imageByte: Long) = imageByte > s3Config.maxImageByte

    private fun getPutObjectRequest(fullFileName: String, imageByte: Long): PutObjectRequest =
        PutObjectRequest.builder()
            .bucket(s3Config.bucket)
            .key(fullFileName)
            .contentLength(imageByte)
            .build()

    private fun getPutObjectPresignRequest(putObjectRequest: PutObjectRequest): PutObjectPresignRequest =
        PutObjectPresignRequest.builder()
            .putObjectRequest(putObjectRequest)
            .signatureDuration(Duration.ofMinutes(s3Config.maxDurationMinute))
            .build()

    private fun presignedUrl(putObjectPresignRequest: PutObjectPresignRequest): String =
        s3Presigner.presignPutObject(putObjectPresignRequest).url().toString()

    private fun getAccessUrl(fullFileName: String): String {
        return String.format("https://%s/%s", s3Config.domain, fullFileName)
    }


}