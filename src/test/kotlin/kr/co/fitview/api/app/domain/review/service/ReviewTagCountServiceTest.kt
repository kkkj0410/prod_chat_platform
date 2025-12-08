package kr.co.fitview.api.app.domain.review.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.repository.ChatRoomRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.review.entity.ReviewCategory
import kr.co.fitview.api.app.domain.review.entity.ReviewTag
import kr.co.fitview.api.app.domain.review.entity.ReviewTagCount
import kr.co.fitview.api.app.domain.review.repository.ReviewCategoryRepository
import kr.co.fitview.api.app.domain.review.repository.ReviewTagCountRepository
import kr.co.fitview.api.app.domain.review.repository.ReviewTagRepository
import kr.co.fitview.api.app.domain.workout_history.repository.WorkoutHistoryRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ReviewTagCountServiceTest @Autowired constructor(
    private val reviewCategoryRepository: ReviewCategoryRepository,
    private val reviewTagRepository : ReviewTagRepository,
    private val reviewTagCountService: ReviewTagCountService,
    private val reviewTagCountRepository: ReviewTagCountRepository,
    private val memberRepository : MemberRepository,

) : IntegrationTestSupport(){

    @DisplayName("회원이 처음받은 리뷰 태그라면 리뷰 태그 집계 데이터를 생성한다.")
    @Test
    fun saveAllReviewTagCount() {
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

        val reviewTagIds = listOf(reviewTag1.id!!, reviewTag2.id!!, reviewTag3.id!!)

        // when
        reviewTagCountService.saveAllReviewTagCount(
            memberId = me.id!!,
            reviewTagIds = reviewTagIds
        )

        // then
        val findReviewTagCounts = reviewTagCountRepository.findAll()
        assertThat(findReviewTagCounts).hasSize(3)
        assertThat(findReviewTagCounts)
            .extracting("member", "reviewTag", "count")
            .contains(
                tuple(me, reviewTag1, 1),
                tuple(me, reviewTag2, 1),
                tuple(me, reviewTag3, 1),
            )
    }

    @DisplayName("회원이 이미받은 리뷰 태그라면 리뷰 태그 집계 데이터에 값을 업데이트한다.")
    @Test
    fun saveReviewTagCountExistsAllReviewTagCount() {
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

        val reviewTagCount = ReviewTagCount(
            member = me,
            reviewTag = reviewTag1,
            count = 2
        )
        reviewTagCountRepository.save(reviewTagCount)

        val reviewTagIds = listOf(reviewTag1.id!!, reviewTag2.id!!, reviewTag3.id!!)

        // when
        reviewTagCountService.saveAllReviewTagCount(
            memberId = me.id!!,
            reviewTagIds = reviewTagIds
        )

        // then
        val findReviewTagCounts = reviewTagCountRepository.findAll()
        assertThat(findReviewTagCounts).hasSize(3)
        assertThat(findReviewTagCounts)
            .extracting("member", "reviewTag", "count")
            .contains(
                tuple(me, reviewTag1, 3),
                tuple(me, reviewTag2, 1),
                tuple(me, reviewTag3, 1),
            )
    }

}