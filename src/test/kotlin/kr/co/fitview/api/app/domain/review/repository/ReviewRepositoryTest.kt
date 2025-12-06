package kr.co.fitview.api.app.domain.review.repository

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.chat.repository.ChatRoomRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.review.dto.request.ReviewCreateServiceRequest
import kr.co.fitview.api.app.domain.review.entity.Review
import kr.co.fitview.api.app.domain.review.entity.ReviewCategory
import kr.co.fitview.api.app.domain.review.entity.ReviewTag
import kr.co.fitview.api.app.domain.review.entity.enums.ReviewType
import kr.co.fitview.api.app.domain.review.service.ReviewService
import kr.co.fitview.api.app.domain.workout_history.entity.WorkoutHistory
import kr.co.fitview.api.app.domain.workout_history.repository.WorkoutHistoryRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal

class ReviewRepositoryTest @Autowired constructor(
    private val reviewCategoryRepository: ReviewCategoryRepository,
    private val reviewTagRepository : ReviewTagRepository,
    private val reviewService : ReviewService,
    private val memberRepository : MemberRepository,
    private val chatRoomRepository : ChatRoomRepository,
    private val workoutHistoryRepository : WorkoutHistoryRepository,
    private val reviewRepository : ReviewRepository,
    private val time : Time
) : IntegrationTestSupport(){

    @DisplayName("해당 회원의 리뷰를 조회한다.")
    @Test
    fun findReviewBy() {
        // given
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
            memberTwo = other,
            completedAt = time.nowLocalDateTime
        )
        workoutHistoryRepository.save(workoutHistory)

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

        val request = ReviewCreateServiceRequest(
            workoutHistoryId = workoutHistory.id!!,
            type = ReviewType.GOOD,
            reviewTagIds = reviewTagIds,
            content = "content"
        )

        reviewService.saveReview(
            memberId = me.id!!,
            request = request
        )

        //when
        val findReview = reviewRepository.findReviewBy(
            memberId = me.id!!,
            workoutHistoryId = workoutHistory.id!!
        )

        //then
        assertThat(findReview).isNotNull()
    }

    @DisplayName("회원 id, 운동 이력 id에 따른 리뷰가 있는지 조회한다.")
    @Test
    fun findByFromMemberIdAndWorkoutHistoryIdAndDeletedAtIsNull() {
        // given
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
            memberTwo = other,
            completedAt = time.nowLocalDateTime
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
        val findReview = reviewRepository.findByFromMemberIdAndWorkoutHistoryIdAndDeletedAtIsNull(me.id!!, workoutHistory.id!!)

        // then
        assertThat(findReview).isNotNull()
    }

}