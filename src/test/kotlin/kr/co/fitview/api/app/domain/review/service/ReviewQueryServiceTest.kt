package kr.co.fitview.api.app.domain.review.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.chat.repository.ChatMessageRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatRoomRepository
import kr.co.fitview.api.app.domain.member.condition.MemberReviewCondition
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.domain.review.entity.Review
import kr.co.fitview.api.app.domain.review.entity.ReviewCategory
import kr.co.fitview.api.app.domain.review.entity.ReviewTag
import kr.co.fitview.api.app.domain.review.entity.enums.ReviewType
import kr.co.fitview.api.app.domain.review.repository.ReviewCategoryRepository
import kr.co.fitview.api.app.domain.review.repository.ReviewRepository
import kr.co.fitview.api.app.domain.review.repository.ReviewTagRelationRepository
import kr.co.fitview.api.app.domain.review.repository.ReviewTagRepository
import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequest
import kr.co.fitview.api.app.domain.workout.repository.WorkoutRequestRepository
import kr.co.fitview.api.app.domain.workout_history.entity.WorkoutHistory
import kr.co.fitview.api.app.domain.workout_history.repository.WorkoutHistoryRepository
import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartner
import kr.co.fitview.api.app.domain.workout_partner.repository.WorkoutPartnerRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import kr.co.fitview.api.app.global.util.TestDataFactory
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
    private val chatMessageRepository : ChatMessageRepository,
    private val workoutRequestRepository : WorkoutRequestRepository,
    private val reviewRepository: ReviewRepository,
    private val reviewCategoryRepository: ReviewCategoryRepository,
    private val reviewTagRepository : ReviewTagRepository,
    private val workoutPartnerRepository : WorkoutPartnerRepository,
    private val oAuth2Service : OAuth2Service,
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

        val chatMessage = ChatMessage(
            member = me,
            chatRoom = chatRoom,
            type = ChatMessageType.WORKOUT_REQUEST,
            content = "content",
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage)

        val workoutRequest = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        workoutRequestRepository.save(workoutRequest)

        val workoutHistory = WorkoutHistory(
            chatRoom = chatRoom,
            workoutRequest = workoutRequest,
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

        val signupRequest = TestDataFactory.oAuth2SignupRequest(
            nickname = "nick1"
        )
        oAuth2Service.signup(signupRequest, me.id!!)

        val signupRequest2 = TestDataFactory.oAuth2SignupRequest(
            nickname = "nick2"
        )
        oAuth2Service.signup(signupRequest2, other.id!!)

        val chatRoom = ChatRoom(ChatRoomType.PRIVATE)
        chatRoomRepository.save(chatRoom)

        val chatMessage = ChatMessage(
            member = me,
            chatRoom = chatRoom,
            type = ChatMessageType.WORKOUT_REQUEST,
            content = "content",
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage)

        val workoutRequest = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        workoutRequestRepository.save(workoutRequest)

        val workoutHistory = WorkoutHistory(
            chatRoom = chatRoom,
            workoutRequest = workoutRequest,
            memberOne = me,
            memberTwo = other,
            completedAt = time.nowLocalDateTime
        )
        workoutHistoryRepository.save(workoutHistory)

        val chatMessage2 = ChatMessage(
            member = me,
            chatRoom = chatRoom,
            type = ChatMessageType.WORKOUT_REQUEST,
            content = "content",
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage2)

        val workoutRequest2 = WorkoutRequest.of(
            chatMessage = chatMessage2,
            fromMember = me,
            toMember = other,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        workoutRequestRepository.save(workoutRequest2)

        val workoutHistory2 = WorkoutHistory(
            chatRoom = chatRoom,
            workoutRequest = workoutRequest2,
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

    @DisplayName("시작일부터 각 일자별 리뷰 개수를 조회한다.")
    @Test
    fun countDistinctDailyReviewFrom() {
        // given
        val member1 = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        val member2 = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(member1)
        memberRepository.save(member2)

        val signupRequest1 = TestDataFactory.oAuth2SignupRequest(
            nickname = "nickname1",
            profileImageUrl = "profile1"
        )
        oAuth2Service.signup(signupRequest1, member1.id!!)

        val signupRequest2 = TestDataFactory.oAuth2SignupRequest(
            nickname = "nickname2",
            profileImageUrl = "profile2"
        )
        oAuth2Service.signup(signupRequest2, member2.id!!)

        val workoutPartner = WorkoutPartner(
            memberOne = member1,
            memberTwo = member2
        )
        workoutPartnerRepository.save(workoutPartner)

        val chatRoom = ChatRoom(ChatRoomType.PRIVATE)
        chatRoomRepository.save(chatRoom)

        val chatMessage = ChatMessage(
            member = member1,
            chatRoom = chatRoom,
            type = ChatMessageType.WORKOUT_REQUEST,
            content = "content",
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage)

        val workoutRequest = WorkoutRequest.of(
            chatMessage = chatMessage,
            fromMember = member1,
            toMember = member2,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        workoutRequestRepository.save(workoutRequest)

        val workoutHistory = WorkoutHistory(
            chatRoom = chatRoom,
            workoutRequest = workoutRequest,
            memberOne = member1,
            memberTwo = member2,
            completedAt = time.nowLocalDateTime
        )
        workoutHistoryRepository.save(workoutHistory)

        val chatMessage2 = ChatMessage(
            member = member1,
            chatRoom = chatRoom,
            type = ChatMessageType.WORKOUT_REQUEST,
            content = "content",
            sentAt = time.nowLocalDateTime.plusDays(1)
        )
        chatMessageRepository.save(chatMessage2)

        val workoutRequest2 = WorkoutRequest.of(
            chatMessage = chatMessage2,
            fromMember = member1,
            toMember = member2,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusDays(1),
            requestedAt = time.nowLocalDateTime
        )
        workoutRequestRepository.save(workoutRequest2)

        val workoutHistory2 = WorkoutHistory(
            chatRoom = chatRoom,
            workoutRequest = workoutRequest2,
            memberOne = member1,
            memberTwo = member2,
            completedAt = time.nowLocalDateTime.plusDays(1)
        )
        workoutHistoryRepository.save(workoutHistory2)

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

        val review = Review(
            fromMember = member1,
            toMember = member2,
            workoutHistory = workoutHistory,
            isPrivate = false,
            type = ReviewType.GOOD,
            score = 2.0,
            content = "content",
            postedAt = time.nowLocalDateTime
        )
        reviewRepository.save(review)

        val review2 = Review(
            fromMember = member1,
            toMember = member2,
            workoutHistory = workoutHistory2,
            isPrivate = false,
            type = ReviewType.GOOD,
            score = 2.0,
            content = "content",
            postedAt = time.nowLocalDateTime.plusDays(1)
        )
        reviewRepository.save(review2)

        // when
        val count = reviewQueryService.countDistinctDailyReviewFrom(
            memberId = member1.id!!,
            startDate = time.nowLocalDate,
            limit = 5
        )

        // then
        assertThat(count).isEqualTo(2L)
    }

}