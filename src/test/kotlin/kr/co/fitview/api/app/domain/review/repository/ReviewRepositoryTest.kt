package kr.co.fitview.api.app.domain.review.repository

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
import kr.co.fitview.api.app.domain.review.condition.AdminReviewCondition
import kr.co.fitview.api.app.domain.review.dto.request.ReviewCreateServiceRequest
import kr.co.fitview.api.app.domain.review.entity.Review
import kr.co.fitview.api.app.domain.review.entity.ReviewCategory
import kr.co.fitview.api.app.domain.review.entity.ReviewTag
import kr.co.fitview.api.app.domain.review.entity.ReviewTagRelation
import kr.co.fitview.api.app.domain.review.entity.enums.ReviewType
import kr.co.fitview.api.app.domain.review.service.ReviewService
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
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.ZoneId

class ReviewRepositoryTest @Autowired constructor(
    private val reviewCategoryRepository: ReviewCategoryRepository,
    private val reviewTagRepository : ReviewTagRepository,
    private val reviewService : ReviewService,
    private val memberRepository : MemberRepository,
    private val chatRoomRepository : ChatRoomRepository,
    private val workoutHistoryRepository : WorkoutHistoryRepository,
    private val reviewRepository : ReviewRepository,
    private val chatMessageRepository : ChatMessageRepository,
    private val workoutRequestRepository : WorkoutRequestRepository,
    private val workoutPartnerRepository : WorkoutPartnerRepository,
    private val reviewTagRelationRepository : ReviewTagRelationRepository,
    private val oAuth2Service : OAuth2Service,
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

        val signupRequest = TestDataFactory.oAuth2SignupRequest()
        oAuth2Service.signup(signupRequest, me.id!!)

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
        val findReview = reviewRepository.findByFromMemberIdAndWorkoutHistoryIdAndDeletedAtIsNull(me.id!!, workoutHistory.id!!)

        // then
        assertThat(findReview).isNotNull()
    }

    @DisplayName("비공개가 걸리지 않은 전체 후기를 조회한다.")
    @Test
    fun findAllPublicReviewByToMemberIdOrderByPostedAtDesc() {
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

        val signupRequest = TestDataFactory.oAuth2SignupRequest(
            nickname = "meNickname",
            profileImageUrl = "updateImage1"
        )
        oAuth2Service.signup(signupRequest, me.id!!)


        val signupRequest2 = TestDataFactory.oAuth2SignupRequest(
            nickname = "otherNick",
            profileImageUrl = "updateImage2"
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
        val response = reviewRepository.findAllPublicReviewByToMemberIdOrderByPostedAtDesc(other.id!!, condition)

        // then
        assertThat(response)
            .extracting("reviewId", "memberId", "nickname", "profileImageUrl", "postedAt", "content")
            .containsExactly(
                tuple(
                    review2.id!!,
                    me.id!!,
                    signupRequest.nickname,
                    signupRequest.profileImageUrl,
                    time.nowLocalDateTime,
                    review2.content
                ),
                tuple(
                    review.id!!,
                    me.id!!,
                    signupRequest.nickname,
                    signupRequest.profileImageUrl,
                    time.nowLocalDateTime.minusDays(2),
                    review.content
                ),
            )
    }

    @DisplayName("공개 후기 조회 시, 삭제된 회원의 후기 정보는 조회하지 않는다.")
    @Test
    fun findAllPublicReviewByToMemberIdOrderByPostedAtDescNotDeletedMember() {
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

        val signupRequest = TestDataFactory.oAuth2SignupRequest(
            nickname = "meNickname",
            profileImageUrl = "updateImage1"
        )
        oAuth2Service.signup(signupRequest, me.id!!)


        val signupRequest2 = TestDataFactory.oAuth2SignupRequest(
            nickname = "otherNick",
            profileImageUrl = "updateImage2"
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

        me.delete(time.nowLocalDateTime)

        val condition = MemberReviewCondition()

        // when
        val response = reviewRepository.findAllPublicReviewByToMemberIdOrderByPostedAtDesc(other.id!!, condition)

        // then
        assertThat(response).hasSize(0)
    }


    @DisplayName("공개 전체 후기 조회 시, 특정 시점 이전의 옛날 후기를 조회한다..")
    @Test
    fun findAllPublicReviewByToMemberIdOrderByPostedAtDescLastPostedAt() {
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

        val signupRequest = TestDataFactory.oAuth2SignupRequest(
            nickname = "meNickname",
            profileImageUrl = "updateImage1"
        )
        oAuth2Service.signup(signupRequest, me.id!!)


        val signupRequest2 = TestDataFactory.oAuth2SignupRequest(
            nickname = "otherNick",
            profileImageUrl = "updateImage2"
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

        val condition = MemberReviewCondition(
            lastPostedAt = time.nowLocalDateTime
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        )

        // when
        val response = reviewRepository.findAllPublicReviewByToMemberIdOrderByPostedAtDesc(other.id!!, condition)

        // then
        assertThat(response)
            .extracting("reviewId", "memberId", "nickname", "profileImageUrl", "postedAt", "content")
            .containsExactly(
                tuple(
                    review.id!!,
                    me.id!!,
                    signupRequest.nickname,
                    signupRequest.profileImageUrl,
                    time.nowLocalDateTime.minusDays(2),
                    review.content
                ),
            )
    }


    @DisplayName("공개 전체 후기 조회 시, 비공개 후기는 조회하지 않는다..")
    @Test
    fun findAllPublicReviewByToMemberIdOrderByPostedAtDescPrivate() {
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

        val signupRequest = TestDataFactory.oAuth2SignupRequest(
            nickname = "meNickname",
            profileImageUrl = "updateImage1"
        )
        oAuth2Service.signup(signupRequest, me.id!!)


        val signupRequest2 = TestDataFactory.oAuth2SignupRequest(
            nickname = "otherNick",
            profileImageUrl = "updateImage2"
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
            isPrivate = true,
            type = ReviewType.GOOD,
            score = 2.0,
            content = "content",
            postedAt = time.nowLocalDateTime
        )
        reviewRepository.save(review2)

        val condition = MemberReviewCondition(
            lastPostedAt = time.nowLocalDateTime
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        )

        // when
        val response = reviewRepository.findAllPublicReviewByToMemberIdOrderByPostedAtDesc(other.id!!, condition)

        // then
        assertThat(response)
            .extracting("reviewId", "memberId", "nickname", "profileImageUrl", "postedAt", "content")
            .containsExactly(
                tuple(
                    review.id!!,
                    me.id!!,
                    signupRequest.nickname,
                    signupRequest.profileImageUrl,
                    time.nowLocalDateTime.minusDays(2),
                    review.content
                ),
            )
    }

    @DisplayName("공개 전체 후기 조회 시, 리뷰 작성글이 없는 글은 조회하지 않는다.")
    @Test
    fun findAllPublicReviewByToMemberIdOrderNotNullContent() {
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

        val signupRequest1 = TestDataFactory.oAuth2SignupRequest(
            nickname = "nickname1",
            profileImageUrl = "profile1"
        )
        oAuth2Service.signup(signupRequest1, me.id!!)

        val signupRequest2 = TestDataFactory.oAuth2SignupRequest(
            nickname = "nickname2",
            profileImageUrl = "profile2"
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
            chatMessage = chatMessage,
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
            isPrivate = true,
            type = ReviewType.GOOD,
            score = 2.0,
            content = null,
            postedAt = time.nowLocalDateTime
        )
        reviewRepository.save(review2)

        val condition = MemberReviewCondition()

        // when
        val response = reviewRepository.findAllPublicReviewByToMemberIdOrderByPostedAtDesc(other.id!!, condition)

        // then
        assertThat(response)
            .extracting("reviewId", "memberId", "nickname", "profileImageUrl", "postedAt", "content")
            .containsExactly(
                tuple(
                    review.id!!,
                    me.id!!,
                    signupRequest1.nickname,
                    signupRequest1.profileImageUrl,
                    time.nowLocalDateTime.minusDays(2),
                    review.content
                ),
            )
    }

    @DisplayName("전체 리뷰를 조회한다.")
    @Test
    fun findAllReviewBy() {
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
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage2)

        val workoutRequest2 = WorkoutRequest.of(
            chatMessage = chatMessage,
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
            completedAt = time.nowLocalDateTime
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
            postedAt = time.nowLocalDateTime.minusDays(2)
        )
        reviewRepository.save(review)

        val review2 = Review(
            fromMember = member1,
            toMember = member2,
            workoutHistory = workoutHistory2,
            isPrivate = true,
            type = ReviewType.GOOD,
            score = 2.0,
            content = null,
            postedAt = time.nowLocalDateTime
        )
        reviewRepository.save(review2)

        val reviewTagRelation1 = ReviewTagRelation(
            review = review,
            reviewTag = reviewTag1,
        )
        val reviewTagRelation2 = ReviewTagRelation(
            review = review,
            reviewTag = reviewTag2,
        )
        val reviewTagRelation3 = ReviewTagRelation(
            review = review2,
            reviewTag = reviewTag3,
        )

        reviewTagRelationRepository.save(reviewTagRelation1)
        reviewTagRelationRepository.save(reviewTagRelation2)
        reviewTagRelationRepository.save(reviewTagRelation3)

        val condition = AdminReviewCondition()

        // when
        val response = reviewRepository.findAllReviewBy(condition)

        // then
        assertThat(response)
            .extracting("reviewId", "workoutPartnerId", "workoutRequestId", "workoutHistoryId", "reviewType", "reviewTagDisplayTexts")
            .containsExactly(
                tuple(
                    review2.id!!,
                    workoutPartner.id!!,
                    workoutRequest2.id!!,
                    workoutHistory2.id!!,
                    ReviewType.GOOD,
                    listOf(reviewTag3.displayText)
                ),
                tuple(
                    review.id!!,
                    workoutPartner.id!!,
                    workoutRequest.id!!,
                    workoutHistory.id!!,
                    ReviewType.GOOD,
                    listOf(reviewTag1.displayText, reviewTag2.displayText)
                ),
            )
    }

    @DisplayName("전체 리뷰를 조회 시, 조건에 id가 있으면 id보다 작은 리뷰를 조회한다.")
    @Test
    fun findAllReviewByExistsConditionReviewId() {
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
            sentAt = time.nowLocalDateTime
        )
        chatMessageRepository.save(chatMessage2)

        val workoutRequest2 = WorkoutRequest.of(
            chatMessage = chatMessage,
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
            completedAt = time.nowLocalDateTime
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
            postedAt = time.nowLocalDateTime.minusDays(2)
        )
        reviewRepository.save(review)

        val review2 = Review(
            fromMember = member1,
            toMember = member2,
            workoutHistory = workoutHistory2,
            isPrivate = true,
            type = ReviewType.GOOD,
            score = 2.0,
            content = null,
            postedAt = time.nowLocalDateTime
        )
        reviewRepository.save(review2)

        val reviewTagRelation1 = ReviewTagRelation(
            review = review,
            reviewTag = reviewTag1,
        )
        val reviewTagRelation2 = ReviewTagRelation(
            review = review,
            reviewTag = reviewTag2,
        )
        val reviewTagRelation3 = ReviewTagRelation(
            review = review2,
            reviewTag = reviewTag3,
        )

        reviewTagRelationRepository.save(reviewTagRelation1)
        reviewTagRelationRepository.save(reviewTagRelation2)
        reviewTagRelationRepository.save(reviewTagRelation3)

        val condition = AdminReviewCondition(
            reviewId = review2.id!!
        )

        // when
        val response = reviewRepository.findAllReviewBy(condition)

        // then
        assertThat(response.content).hasSize(1)
        assertThat(response.content[0].reviewId).isEqualTo(review.id!!)
        assertThat(response.hasNext()).isEqualTo(false)
    }

    @DisplayName("시작일부터 각 일자별 리뷰 개수를 조회한다.")
    @Test
    fun countDistinctDailyReviewBy() {
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
        val count = reviewRepository.countDistinctDailyReviewBy(
            startDate = time.nowLocalDate,
            limit = 5
        )

        // then
        assertThat(count).isEqualTo(2L)
    }

    @DisplayName("일자별 리뷰 개수 조회 시, 시작일 이전 리뷰는 조회하지 않는다.")
    @Test
    fun countDistinctDailyReviewByNotFindBeforeDate() {
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
        val count = reviewRepository.countDistinctDailyReviewBy(
            startDate = time.nowLocalDate.plusDays(1),
            limit = 5
        )

        // then
        assertThat(count).isEqualTo(1L)
    }

    @DisplayName("일자별 리뷰 개수 조회 시, 제한 개수를 넘어서면 조회에서 제외한다.")
    @Test
    fun countDistinctDailyReviewByLimit() {
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
        val count = reviewRepository.countDistinctDailyReviewBy(
            startDate = time.nowLocalDate,
            limit = 1
        )

        // then
        assertThat(count).isEqualTo(1L)
    }

    @DisplayName("일자별 리뷰 개수 조회 시, 같은날의 운동 기록 중복은 개수 1개로 취급한다.")
    @Test
    fun countDistinctDailyReviewByDuplicateDailyWorkoutHistory() {
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
            scheduledAt = time.nowLocalDateTime,
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
            sentAt = time.nowLocalDateTime.plusSeconds(1)
        )
        chatMessageRepository.save(chatMessage2)

        val workoutRequest2 = WorkoutRequest.of(
            chatMessage = chatMessage2,
            fromMember = member1,
            toMember = member2,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusSeconds(1),
            requestedAt = time.nowLocalDateTime
        )
        workoutRequestRepository.save(workoutRequest2)

        val workoutHistory2 = WorkoutHistory(
            chatRoom = chatRoom,
            workoutRequest = workoutRequest2,
            memberOne = member1,
            memberTwo = member2,
            completedAt = time.nowLocalDateTime.plusSeconds(1)
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
            postedAt = time.nowLocalDateTime
        )
        reviewRepository.save(review2)

        // when
        val count = reviewRepository.countDistinctDailyReviewBy(
            startDate = time.nowLocalDate,
            limit = 5
        )

        // then
        assertThat(count).isEqualTo(1L)
    }

    @DisplayName("일자별 리뷰 개수 조회 시, 리뷰 날짜가 다르더라도 운동 기록일이 같으면 1개 count로 제한한다.")
    @Test
    fun countDistinctDailyReviewByOtherReviewDuplicateWorkoutHistory() {
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
            scheduledAt = time.nowLocalDateTime,
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
            sentAt = time.nowLocalDateTime.plusSeconds(1)
        )
        chatMessageRepository.save(chatMessage2)

        val workoutRequest2 = WorkoutRequest.of(
            chatMessage = chatMessage2,
            fromMember = member1,
            toMember = member2,
            location = "location",
            scheduledAt = time.nowLocalDateTime.plusSeconds(1),
            requestedAt = time.nowLocalDateTime
        )
        workoutRequestRepository.save(workoutRequest2)

        val workoutHistory2 = WorkoutHistory(
            chatRoom = chatRoom,
            workoutRequest = workoutRequest2,
            memberOne = member1,
            memberTwo = member2,
            completedAt = time.nowLocalDateTime.plusSeconds(1)
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
        val count = reviewRepository.countDistinctDailyReviewBy(
            startDate = time.nowLocalDate,
            limit = 5
        )

        // then
        assertThat(count).isEqualTo(1L)
    }
}