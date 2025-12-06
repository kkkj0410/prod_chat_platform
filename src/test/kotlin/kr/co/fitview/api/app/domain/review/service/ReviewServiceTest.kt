package kr.co.fitview.api.app.domain.review.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.chat.repository.ChatRoomRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.review.dto.request.ReviewCreateServiceRequest
import kr.co.fitview.api.app.domain.review.entity.ReviewCategory
import kr.co.fitview.api.app.domain.review.entity.ReviewTag
import kr.co.fitview.api.app.domain.review.entity.enums.ReviewType
import kr.co.fitview.api.app.domain.review.repository.ReviewCategoryRepository
import kr.co.fitview.api.app.domain.review.repository.ReviewTagRepository
import kr.co.fitview.api.app.domain.workout_history.entity.WorkoutHistory
import kr.co.fitview.api.app.domain.workout_history.repository.WorkoutHistoryRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.review.ReviewErrorCode
import kr.co.fitview.api.app.global.time.Time
import org.assertj.core.api.Assertions.*
import org.assertj.core.api.ThrowingConsumer
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal

class ReviewServiceTest @Autowired constructor(
    private val reviewCategoryRepository: ReviewCategoryRepository,
    private val reviewTagRepository : ReviewTagRepository,
    private val reviewService : ReviewService,
    private val memberRepository : MemberRepository,
    private val chatRoomRepository : ChatRoomRepository,
    private val workoutHistoryRepository : WorkoutHistoryRepository,
    private val time : Time
) : IntegrationTestSupport(){


    @DisplayName("회원이 사용가능한 전체 후기 선택지를 조회한다.")
    @Test
    fun findReviewCategoryAndTag() {
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
        val findTags = reviewService.findReviewCategoryAndTag()

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

    @DisplayName("리뷰를 저장한다.")
    @Test
    fun saveReview() {
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

        // when
        val savedReview = reviewService.saveReview(
            memberId = me.id!!,
            request = request
        )

        // then
        assertThat(savedReview)
            .extracting("fromMember", "toMember", "workoutHistory", "isPrivate", "type", "score", "content")
            .contains(me, other, workoutHistory, false, ReviewType.GOOD, BigDecimal(1), "content")
    }

    @DisplayName("악평 리뷰를 저장한다.")
    @Test
    fun saveReviewNegative() {
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

        val request = ReviewCreateServiceRequest(
            workoutHistoryId = workoutHistory.id!!,
            type = ReviewType.BAD,
            content = "content"
        )

        // when
        val savedReview = reviewService.saveReview(
            memberId = me.id!!,
            request = request
        )

        // then
        assertThat(savedReview)
            .extracting("fromMember", "toMember", "workoutHistory", "isPrivate", "type", "score", "content")
            .contains(me, other, workoutHistory, true, ReviewType.BAD, BigDecimal(-2), "content")
    }

    @DisplayName("긍정 리뷰를 저장하는데 서브 메시지가 없으면 리뷰를 저장하지 않는다.")
    @Test
    fun saveReviewNullReviewTagIds() {
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

        val request = ReviewCreateServiceRequest(
            workoutHistoryId = workoutHistory.id!!,
            type = ReviewType.GOOD,
            reviewTagIds = null,
            content = "content"
        )

        // when & then
        assertThatThrownBy {
            reviewService.saveReview(
                memberId = me.id!!,
                request = request
            )
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(ReviewErrorCode.POSITIVE_REVIEW_SUB_SELECTION_REQUIRED)
            })
    }

    @DisplayName("악평 리뷰를 저장하는데 서브 메시지를 주면 리뷰를 저장하지 않는다.")
    @Test
    fun saveReviewNegativeWithReviewTagIds() {
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
            type = ReviewType.BAD,
            reviewTagIds = reviewTagIds,
            content = "content"
        )

        // when & then
        assertThatThrownBy {
            reviewService.saveReview(
                memberId = me.id!!,
                request = request
            )
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(ReviewErrorCode.NEGATIVE_REVIEW_SUB_SELECTION_NOT_ALLOWED)
            })
    }

    @DisplayName("리뷰를 저장 시, 상대 회원 평점이 새로 반영된다.")
    @Test
    fun saveReviewMemberScore() {
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

        // when
        reviewService.saveReview(
            memberId = me.id!!,
            request = request
        )

        // then
        assertThat(other.score).isEqualTo(37.0)
    }

    @DisplayName("이미 해당 운동 기록에 대한 리뷰를 작성했으면 다시 리뷰를 작성할 수 없다.")
    @Test
    fun saveReviewDuplicatedReview() {
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

        // when & then
        assertThatThrownBy {
            reviewService.saveReview(
                memberId = me.id!!,
                request = request
            )
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(ReviewErrorCode.REVIEW_ALREADY_EXISTS)
            })

    }

    @DisplayName("리뷰 작성 시, 운동 이력이 없으면 리뷰 작성이 불가하다.")
    @Test
    fun saveReviewNotWorkoutHistoryId() {
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
            workoutHistoryId = 123,
            type = ReviewType.GOOD,
            reviewTagIds = reviewTagIds,
            content = "content"
        )


        // when & then
        assertThatThrownBy {
            reviewService.saveReview(
                memberId = me.id!!,
                request = request
            )
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(ReviewErrorCode.WORKOUT_HISTORY_NOT_FOUND)
            })

    }
}