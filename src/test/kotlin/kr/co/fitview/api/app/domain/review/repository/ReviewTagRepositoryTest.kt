package kr.co.fitview.api.app.domain.review.repository

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.review.entity.ReviewCategory
import kr.co.fitview.api.app.domain.review.entity.ReviewTag
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ReviewTagRepositoryTest @Autowired constructor(
    private val reviewCategoryRepository: ReviewCategoryRepository,
    private val reviewTagRepository : ReviewTagRepository
) : IntegrationTestSupport(){

    @DisplayName("회원이 사용가능한 전체 후기 선택지를 조회한다.")
    @Test
    fun findAllReviewTag() {
        // given
        val reviewCategory1 = ReviewCategory(
            displayText = "displayText1",
            seq = 100
        )
        val reviewCategory2 = ReviewCategory(
            displayText = "displayText1",
            seq = 200
        )
        reviewCategoryRepository.save(reviewCategory1)
        reviewCategoryRepository.save(reviewCategory2)

        val reviewTag1 = ReviewTag(
            reviewCategory = reviewCategory1,
            displayText = "tagText1",
            seq = 100
        )
        val reviewTag2 = ReviewTag(
            reviewCategory = reviewCategory1,
            displayText = "tagText2",
            seq = 200
        )
        val reviewTag3 = ReviewTag(
            reviewCategory = reviewCategory2,
            displayText = "tagText3",
            seq = 100
        )
        reviewTagRepository.save(reviewTag1)
        reviewTagRepository.save(reviewTag2)
        reviewTagRepository.save(reviewTag3)

        // when
        val findTags = reviewTagRepository.findAllReviewTag()

        // then
        assertThat(findTags).hasSize(2)

        assertThat(findTags)
            .extracting("reviewCategoryId", "reviewCategoryDisplayText")
            .containsExactly(
                tuple(reviewCategory1.id, reviewCategory1.displayText),
                tuple(reviewCategory2.id, reviewCategory2.displayText)
            )

        assertThat(findTags[0].tags)
            .extracting("reviewTagId", "reviewTagDisplayText")
            .containsExactly(
                tuple(reviewTag1.id, reviewTag1.displayText),
                tuple(reviewTag2.id, reviewTag2.displayText)
            )

        assertThat(findTags[1].tags)
            .extracting("reviewTagId", "reviewTagDisplayText")
            .containsExactly(
                tuple(reviewTag3.id, reviewTag3.displayText)
            )
    }

}