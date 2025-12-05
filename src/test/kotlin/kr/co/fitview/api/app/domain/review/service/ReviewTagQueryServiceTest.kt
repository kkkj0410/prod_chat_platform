package kr.co.fitview.api.app.domain.review.service

import jakarta.persistence.EntityManager
import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.entity.QChatRoom.chatRoom
import kr.co.fitview.api.app.domain.review.entity.QReviewTag.reviewTag
import kr.co.fitview.api.app.domain.review.entity.ReviewCategory
import kr.co.fitview.api.app.domain.review.entity.ReviewTag
import kr.co.fitview.api.app.domain.review.repository.ReviewCategoryRepository
import kr.co.fitview.api.app.domain.review.repository.ReviewTagRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.hibernate.proxy.HibernateProxy
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ReviewTagQueryServiceTest @Autowired constructor(
    private val reviewTagQueryService : ReviewTagQueryService,
    private val reviewCategoryRepository : ReviewCategoryRepository,
    private val reviewTagRepository : ReviewTagRepository,
    private val em : EntityManager
) : IntegrationTestSupport(){


    @DisplayName("모든 리뷰 태그를 프록시로 조회한다.")
    @Test
    fun findAllReviewTagReferenceFrom() {
        // given
        val reviewCategory = ReviewCategory(
            displayText = "displayText",
            seq = 100
        )
        reviewCategoryRepository.save(reviewCategory)

        val reviewTag1 = ReviewTag(
            reviewCategory = reviewCategory,
            displayText = "displayText1",
            seq = 100
        )
        val reviewTag2 = ReviewTag(
            reviewCategory = reviewCategory,
            displayText = "displayText2",
            seq = 200
        )
        reviewTagRepository.save(reviewTag1)
        reviewTagRepository.save(reviewTag2)

        val reviewTagIds = listOf(reviewTag1.id!!, reviewTag2.id!!)

        em.flush()
        em.clear()

        // when
        val findReviewTags = reviewTagQueryService.findAllReviewTagReferenceFrom(reviewTagIds)

        // then
        assertThat(findReviewTags).hasSize(2)

        assertThat(findReviewTags[0]).isInstanceOf(HibernateProxy::class.java)
        assertThat(findReviewTags[1]).isInstanceOf(HibernateProxy::class.java)

        assertThat(findReviewTags)
            .extracting("displayText", "seq")
            .containsExactlyInAnyOrder(
                tuple("displayText1", 100),
                tuple("displayText2", 200),
            )
    }
}