package kr.co.fitview.api.app.domain.review.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.chat.repository.ChatRoomRepository
import kr.co.fitview.api.app.domain.member.condition.MemberReviewCondition
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.review.entity.Review
import kr.co.fitview.api.app.domain.review.entity.enums.ReviewType
import kr.co.fitview.api.app.domain.review.repository.ReviewCategoryRepository
import kr.co.fitview.api.app.domain.review.repository.ReviewRepository
import kr.co.fitview.api.app.domain.review.repository.ReviewTagRepository
import kr.co.fitview.api.app.domain.workout_history.entity.WorkoutHistory
import kr.co.fitview.api.app.domain.workout_history.repository.WorkoutHistoryRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.ZoneId

class ReviewQueryServiceTest @Autowired constructor(
    private val reviewQueryService: ReviewQueryService,
    private val memberRepository: MemberRepository,
    private val chatRoomRepository: ChatRoomRepository,
    private val workoutHistoryRepository: WorkoutHistoryRepository,
    private val reviewRepository: ReviewRepository,
    private val time: Time
) : IntegrationTestSupport() {


    @DisplayName("회원 id, 운동 이력 id에 따른 리뷰가 있는지 조회한다.")
    @Test
    fun findReviewFrom() {
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
            score = 2.0,
            content = "content",
            postedAt = time.nowLocalDateTime
        )

        reviewRepository.save(review)

        // when
        val findReview = reviewQueryService.findReviewFrom(me.id!!, workoutHistory.id!!)

        // then
        assertThat(findReview).isNotNull()
    }

    @DisplayName("비공개가 걸리지 않은 전체 후기를 조회한다.")
    @Test
    fun findReviewFromCondition() {
        // given
        val me = Member(
            email = "email",
            password = "password",
            role = Role.USER,
            nickname = "nickname"
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

        val workoutHistory2 = WorkoutHistory(
            chatRoom = chatRoom,
            memberOne = me,
            memberTwo = other,
            completedAt = time.nowLocalDateTime
        )
        workoutHistoryRepository.save(workoutHistory2)

        val review = Review(
            fromMember = me,
            toMember = other,
            workoutHistory = workoutHistory,
            isPrivate = false,
            type = ReviewType.GOOD,
            score = 2.0,
            content = "content",
            postedAt = time.nowLocalDateTime.minusDays(2)
        )
        reviewRepository.save(review)

        val review2 = Review(
            fromMember = me,
            toMember = other,
            workoutHistory = workoutHistory2,
            isPrivate = false,
            type = ReviewType.GOOD,
            score = 2.0,
            content = "content",
            postedAt = time.nowLocalDateTime
        )
        reviewRepository.save(review2)

        val condition = MemberReviewCondition()

        // when
        val findReviews = reviewQueryService.findReviewFromCondition(other.id!!, condition)

        // then
        assertThat(findReviews)
            .extracting("reviewId", "memberId", "nickname", "postedAt", "content")
            .containsExactly(
                tuple(review2.id!!, me.id!!, me.nickname, time.nowLocalDateTime, review2.content),
                tuple(review.id!!, me.id!!, me.nickname, time.nowLocalDateTime.minusDays(2), review.content),
            )
    }

}