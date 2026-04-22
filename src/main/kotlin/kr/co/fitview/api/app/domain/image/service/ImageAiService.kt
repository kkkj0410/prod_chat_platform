package kr.co.fitview.api.app.domain.image.service

import kr.co.fitview.api.app.domain.image.dto.response.ImageFilteringResponse
import kr.co.fitview.api.app.global.ai.service.AiChatClient
import kr.co.fitview.api.app.global.ai.service.ImageModerationService
import kr.co.fitview.api.app.global.ai.service.ImageModerationService.ModerationAnalysis
import org.springframework.stereotype.Service

@Service
class ImageAiService(
    private val s3Service: S3Service,
    private val aiChatClient: AiChatClient,
    private val imageModerationService: ImageModerationService
) {

    companion object {
        private const val SYSTEM_PROMPT = """
        당신은 피트니스 및 체형 분석 전문 AI 퍼스널 트레이너입니다.
        사용자가 자신의 신체 사진(눈바디, 근육 발달 상태, 다이어트 경과 등)을 업로드하면, 사진을 분석하여 동기를 부여하는 피드백을 제공해야 합니다.
        
        [피드백 작성 가이드라인]
        - 정상적인 눈바디/운동 사진인 경우: 사진에 드러난 체형, 근육(복근, 등, 어깨 등), 자세 등의 특징을 바탕으로 구체적인 칭찬과 응원의 메시지를 작성하세요. 전문가답게 친절하고 활기찬 톤을 유지하세요.
        - 피트니스와 무관한 사진(음식, 풍경, 가려진 사진 등)인 경우: "체형을 정확히 확인하기 어려운 사진이에요. 몸이 잘 보이는 눈바디 사진을 올려주시면 더 정확한 피드백을 해드릴게요!"와 같이 부드럽게 다시 업로드하도록 안내하세요.
        
        결과는 반드시 아래 JSON 형식으로만 반환하세요.
        {
            "message": "사용자에게 그대로 보여줄 완성된 피드백 메시지 (한국어)"
        }
        """

        private const val USER_PROMPT = "이 이미지가 음란물인지 확인해줘."
    }


    fun filterImage1(imageUrl: String): ModerationAnalysis {
        val imageResource = s3Service.downloadImage(imageUrl)
        val imageBytes = imageResource.contentAsByteArray

        // 2. ImageModerationService를 통해 유해성 판별 결과를 가져옵니다.
        val moderationResult = imageModerationService.analyzeImage(imageBytes)

        // 3. 결과를 ImageFilteringResponse 형식에 맞게 매핑하여 반환합니다.
//        return ImageFilteringResponse(
//            isExplicit = moderationResult.isExplicit,
//            reason = moderationResult.buildReasonMessage()
//        )
        return moderationResult
    }

    fun filterImage2(imageUrl: String): ImageFilteringResponse {
        val imageResource = s3Service.downloadImage(imageUrl)

        val response = aiChatClient.callWithImage(
            systemPrompt = SYSTEM_PROMPT,
            userPrompt = USER_PROMPT,
            imageResource = imageResource,
            responseType = AiFilterResult::class.java
        )

        return ImageFilteringResponse(
            message = response.result.message,
            tokenUsage = response.tokenUsage
        )


    }

    data class AiFilterResult(
        val message: String
    )
}
