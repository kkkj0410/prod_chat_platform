package kr.co.fitview.api.app.domain.app_feedback.service

import kr.co.fitview.api.app.domain.member.dto.response.MemberRecommendationAppFeedback
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.random.Random


@Service
@Transactional(readOnly = true)
class AppFeedbackQueryService {


    fun findAppFeedbackCard(): MemberRecommendationAppFeedback? {
        if (Random.nextBoolean()) {
            return null
        }

        return MemberRecommendationAppFeedback(
            positionIndex = 2,
            imageUrl = "https://static-dev.fitview.co.kr/app-feedback/card/9a6bd005-d2cc-432b-81ab-d45ad1a5b86c"
        )
    }
}