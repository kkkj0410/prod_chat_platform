package kr.co.fitview.api.app.domain.image.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.image.config.S3Config
import kr.co.fitview.api.app.domain.image.dto.request.S3UploadUrlServiceRequest
import kr.co.fitview.api.app.domain.image.enums.S3Prefix
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.image.ImageErrorCode
import kr.co.fitview.api.app.global.id.TestIdGenerator
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.ThrowingConsumer
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import software.amazon.awssdk.services.s3.presigner.S3Presigner


class S3ServiceTest @Autowired constructor(
    val s3Presigner: S3Presigner
)
    : IntegrationTestSupport(){

    private fun createS3Config(
        accessKey: String = "accessKey",
        secretKey: String = "secretKey",
        region: String = "region",
        bucket: String = "bucket",
        domain: String = "domain",
        maxImageByte: Long = 52428800L,
        maxImageCount: Int = 50,
        maxDurationMinute: Long = 10L
    ): S3Config {
        return S3Config(
            accessKey = accessKey,
            secretKey = secretKey,
            region = region,
            bucket = bucket,
            domain = domain,
            maxImageByte = maxImageByte,
            maxImageCount = maxImageCount,
            maxDurationMinute = maxDurationMinute,
            endpoint = null
        )
    }

    private fun createS3Service(
        domain: String = "domain",
        maxImageByte: Long = 52428800L,
        maxImageCount: Int = 50,
        maxDurationMinute: Long = 10L,
        uuid: String = "uuid"
    ): S3Service {
        val config = createS3Config(
            domain = domain,
            maxImageByte = maxImageByte,
            maxImageCount = maxImageCount,
            maxDurationMinute = maxDurationMinute
        )
        return S3Service(config, s3Presigner, TestIdGenerator(uuid))
    }


    @DisplayName("이미지 업로드 url을 요청하면 presigned url을 제공한다.")
    @Test
    fun getAllUploadUrls() {
        // given
        val domain = "domain"
        val uuid = "uuid"
        val maxDurationMinute = 5L

        val s3Service = createS3Service(
            domain = domain,
            uuid = uuid,
            maxDurationMinute = maxDurationMinute
        )

        val requests = listOf(
            S3UploadUrlServiceRequest(S3Prefix.MEMBER_PROFILE, 100L),
            S3UploadUrlServiceRequest(S3Prefix.MEMBER_PROFILE, 200L),
            S3UploadUrlServiceRequest(S3Prefix.MEMBER_PROFILE, 300L),
        )

        val fullFileName = domain + "/" + S3Prefix.MEMBER_PROFILE.value + "/" + uuid

        // when
        val responses = s3Service.getAllUploadUrls(requests)

        // then
        assertThat(responses).hasSize(3)
        assertThat(responses)
            .anySatisfy  { res ->
                assertThat(res.presignedUrl).isNotNull
                assertThat(res.presignedUrl).startsWith("https://bucket.s3")
                assertThat(res.presignedUrl).contains("X-Amz-Signature")
                assertThat(res.presignedUrl).contains("X-Amz-Expires=${maxDurationMinute * 60}")

                assertThat(res.accessUrl).contains(fullFileName)
            }
    }

    @DisplayName("이미지 업로드 url 요청 시, 파일 용량이 허용량을 넘어서면 요청을 거부한다.")
    @Test
    fun getAllUploadUrlsFileTooLarge() {
        // given
        val maxImageByte = 52428800L
        val exceedImageByte = 52428801L

        val s3Service = createS3Service(
            maxImageByte = maxImageByte
        )

        val requests = listOf(
            S3UploadUrlServiceRequest(S3Prefix.MEMBER_PROFILE, exceedImageByte),
        )

        // when & then
        assertThatThrownBy {
            s3Service.getAllUploadUrls(requests)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(ImageErrorCode.S3_IMAGE_TOO_LARGE)
            })
    }

    @DisplayName("이미지 업로드 url 요청 시, 파일 개수가 허용량을 넘어서면 요청을 거부한다.")
    @Test
    fun getAllUploadUrlsFileTooMany() {
        // given
        val maxImageCount = 3

        val s3Service = createS3Service(
            maxImageCount = maxImageCount
        )

        val requests = listOf(
            S3UploadUrlServiceRequest(S3Prefix.MEMBER_PROFILE, 100L),
            S3UploadUrlServiceRequest(S3Prefix.MEMBER_PROFILE, 200L),
            S3UploadUrlServiceRequest(S3Prefix.MEMBER_PROFILE, 300L),
            S3UploadUrlServiceRequest(S3Prefix.MEMBER_PROFILE, 400L),
        )

        // when & then
        assertThatThrownBy {
            s3Service.getAllUploadUrls(requests)
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(ImageErrorCode.S3_UPLOAD_COUNT_LIMIT)
            })
    }

}