package kr.co.fitview.api.app.global.ai.service

import com.google.cloud.vision.v1.AnnotateImageRequest
import com.google.cloud.vision.v1.Feature
import com.google.cloud.vision.v1.Image
import com.google.cloud.vision.v1.ImageAnnotatorClient
import com.google.cloud.vision.v1.Likelihood
import com.google.protobuf.ByteString
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class ImageModerationService(
    private val visionClient: ImageAnnotatorClient
) {
    // 차단 기준: LIKELY(높음) 또는 VERY_LIKELY(매우 높음)일 경우 유해물로 간주
    private val BLOCK_CRITERIA = setOf(Likelihood.VERY_LIKELY)

    fun analyzeImage(imageBytes: ByteArray): ModerationAnalysis {
        val img = Image.newBuilder().setContent(ByteString.copyFrom(imageBytes)).build()
        val feat = Feature.newBuilder().setType(Feature.Type.SAFE_SEARCH_DETECTION).build()
        val request = AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build()

        val response = visionClient.batchAnnotateImages(listOf(request))
        val safeSearch = response.responsesList[0].safeSearchAnnotation

        val isAdult = BLOCK_CRITERIA.contains(safeSearch.adult)

        return ModerationAnalysis(
            isAdult = isAdult,
            adultLikelihood = safeSearch.adult,
            racyLikelihood = safeSearch.racy
        )
    }

    data class ModerationAnalysis(
        val isAdult: Boolean,
        val adultLikelihood: Likelihood,
        val racyLikelihood: Likelihood
    ) {
        fun buildReasonMessage(): String {
            return if (isAdult) {
                "검측 결과: 성인물(${adultLikelihood}), 선정성(${racyLikelihood}) 수치가 높아 차단되었습니다."
            } else {
                "정상적인 이미지로 판단됩니다. (성인물: ${adultLikelihood}, 선정성: ${racyLikelihood})"
            }
        }
    }
}