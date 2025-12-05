package kr.co.fitview.api.app.domain.review.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.chat.repository.ChatRoomRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.review.entity.QReview.review
import kr.co.fitview.api.app.domain.review.entity.Review
import kr.co.fitview.api.app.domain.review.entity.ReviewCategory
import kr.co.fitview.api.app.domain.review.entity.ReviewTag
import kr.co.fitview.api.app.domain.review.entity.enums.ReviewType
import kr.co.fitview.api.app.domain.review.repository.ReviewCategoryRepository
import kr.co.fitview.api.app.domain.review.repository.ReviewRepository
import kr.co.fitview.api.app.domain.review.repository.ReviewTagRelationRepository
import kr.co.fitview.api.app.domain.review.repository.ReviewTagRepository
import kr.co.fitview.api.app.domain.workout.entity.WorkoutHistory
import kr.co.fitview.api.app.domain.workout.repository.WorkoutHistoryRepository
import kr.co.fitview.api.app.global.entity.Role
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal

class ReviewTagRelationServiceTest @Autowired constructor(
    private val reviewTagRelationRepository : ReviewTagRelationRepository,
    private val reviewTagQueryService : ReviewTagQueryService,
    private val reviewRepository : ReviewRepository,
    private val reviewCategoryRepository : ReviewCategoryRepository,
    private val reviewTagRepository : ReviewTagRepository,
    private val reviewTagRelationService: ReviewTagRelationService,
    private val memberRepository : MemberRepository,
    private val chatRoomRepository : ChatRoomRepository,
    private val workoutHistoryRepository : WorkoutHistoryRepository
) : IntegrationTestSupport(){


    @DisplayName("회원의 전체 서브 메시지를 저장한다.")
    @Test
    fun addAllReviewTagRelationFrom() {
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

        val me = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        val other = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(me)
        memberRepository.save(other)

        val chatRoom = ChatRoom(ChatRoomType.PRIVATE)
        chatRoomRepository.save(chatRoom)

        val workoutHistory = WorkoutHistory(
            chatRoom = chatRoom,
            memberOne = me,
            memberTwo = other
        )
        workoutHistoryRepository.save(workoutHistory)

        val review = Review(
            fromMember = me,
            toMember = other,
            workoutHistory = workoutHistory,
            isPrivate = false,
            type = ReviewType.GOOD,
            score = BigDecimal(2),
            content = "content"
        )
        reviewRepository.save(review)


        // when
        reviewTagRelationService.addAllReviewTagRelationFrom(
            review = review,
            reviewTagIds = reviewTagIds
        )

        // then
        val findReviewTagRelations = reviewTagRelationRepository.findAll()
        assertThat(findReviewTagRelations).hasSize(2)
    }
}