package kr.co.fitview.api.app.domain.review.repository

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.review.entity.ReviewCategory
import kr.co.fitview.api.app.domain.review.entity.ReviewTag
import kr.co.fitview.api.app.domain.review.entity.ReviewTagCount
import kr.co.fitview.api.app.global.entity.Role
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ReviewTagCountRepositoryTest @Autowired constructor(
    private val reviewCategoryRepository: ReviewCategoryRepository,
    private val reviewTagRepository : ReviewTagRepository,
    private val reviewTagCountRepository : ReviewTagCountRepository,
    private val memberRepository : MemberRepository
) : IntegrationTestSupport(){

    @DisplayName("회원의 리뷰 태그 개수를 조회한다.")
    @Test
    fun findByMemberIdAndReviewTagIdIn() {
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

        val me = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(me)

        val reviewTagCounts = listOf(
            ReviewTagCount.of(
                member = me,
                reviewTag = reviewTag1,
            ),
            ReviewTagCount.of(
                member = me,
                reviewTag = reviewTag2,
            ),
            ReviewTagCount.of(
                member = me,
                reviewTag = reviewTag3,
            ),
        )
        reviewTagCountRepository.saveAll(reviewTagCounts)

        val reviewTagIds = listOf(reviewTag1.id!!, reviewTag2.id!!, reviewTag3.id!!)

        // when
        val findTagCounts = reviewTagCountRepository.findByMemberIdAndReviewTagIdIn(me.id!!, reviewTagIds)

        // then
        assertThat(findTagCounts).hasSize(3)
        assertThat(findTagCounts)
            .extracting("member", "reviewTag", "count")
            .contains(
                tuple(me, reviewTag1, 1),
                tuple(me, reviewTag2, 1),
                tuple(me, reviewTag3, 1),
            )
    }


    @DisplayName("회원이 받은 전체 리뷰 태그 메시지를 조회한다.")
    @Test
    fun findAllReviewTagCountByMemberId() {
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

        val me = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(me)

        val reviewTagCounts = listOf(
            ReviewTagCount.of(
                member = me,
                reviewTag = reviewTag1,
            ),
            ReviewTagCount.of(
                member = me,
                reviewTag = reviewTag2,
            ),
            ReviewTagCount.of(
                member = me,
                reviewTag = reviewTag3,
            ),
        )
        reviewTagCountRepository.saveAll(reviewTagCounts)

        //when
        val response = reviewTagCountRepository.findAllReviewTagCountByMemberId(me.id!!)

        // then
        assertThat(response)
            .extracting("displayText", "count")
            .contains(
                tuple("tagText1", 1),
                tuple("tagText2", 1),
                tuple("tagText3", 1),
            )
    }

}