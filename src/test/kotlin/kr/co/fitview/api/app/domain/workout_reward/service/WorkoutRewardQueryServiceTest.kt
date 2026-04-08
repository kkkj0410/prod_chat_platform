package kr.co.fitview.api.app.domain.workout_reward.service

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.entity.ChatMessage
import kr.co.fitview.api.app.domain.chat.entity.ChatRoom
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatMessageType
import kr.co.fitview.api.app.domain.chat.entity.enums.ChatRoomType
import kr.co.fitview.api.app.domain.chat.repository.ChatMessageRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatRoomRepository
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
import kr.co.fitview.api.app.domain.review.service.ReviewService
import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequest
import kr.co.fitview.api.app.domain.workout.repository.WorkoutRequestRepository
import kr.co.fitview.api.app.domain.workout_history.entity.WorkoutHistory
import kr.co.fitview.api.app.domain.workout_history.repository.WorkoutHistoryRepository
import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartner
import kr.co.fitview.api.app.domain.workout_partner.repository.WorkoutPartnerRepository
import kr.co.fitview.api.app.domain.workout_reward.dto.response.WorkoutRewardCouponStatusResponse
import kr.co.fitview.api.app.domain.workout_reward.entity.WorkoutRewardClaim
import kr.co.fitview.api.app.domain.workout_reward.entity.enums.WorkoutRewardClaimCouponStatus
import kr.co.fitview.api.app.domain.workout_reward.entity.enums.WorkoutRewardClaimWorkoutCount
import kr.co.fitview.api.app.domain.workout_reward.repository.WorkoutRewardClaimRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import kr.co.fitview.api.app.global.util.TestDataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class WorkoutRewardQueryServiceTest @Autowired constructor(
    private val workoutRewardQueryService : WorkoutRewardQueryService,
    private val reviewCategoryRepository: ReviewCategoryRepository,
    private val reviewTagRepository : ReviewTagRepository,
    private val memberRepository : MemberRepository,
    private val chatRoomRepository : ChatRoomRepository,
    private val workoutHistoryRepository : WorkoutHistoryRepository,
    private val reviewRepository : ReviewRepository,
    private val chatMessageRepository : ChatMessageRepository,
    private val workoutRequestRepository : WorkoutRequestRepository,
    private val workoutPartnerRepository : WorkoutPartnerRepository,
    private val workoutRewardClaimRepository: WorkoutRewardClaimRepository,
    private val oAuth2Service : OAuth2Service,
    private val time : Time
) : IntegrationTestSupport(){

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
        val response = workoutRewardQueryService.findWorkoutRewardStamp(
            memberId = member1.id!!,
            startDate = time.nowLocalDate,
            limit = 5
        )

        // then
        assertThat(response.stampCount).isEqualTo(2)
    }

    @DisplayName("리워드 보상 쿠폰 상태 조회 - 모두 지급 가능")
    @Test
    fun findWorkoutRewardCouponStatusAllClaimable() {
        // given
        val member1 = Member(
            email = "email1",
            password = "password",
            role = Role.USER
        )
        val member2 = Member(
            email = "email2",
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

        val reviewTag1 = ReviewTag(reviewCategory = reviewCategory1, displayText = "tagText1", seq = 100)
        val reviewTag2 = ReviewTag(reviewCategory = reviewCategory1, displayText = "tagText2", seq = 200)
        val reviewTag3 = ReviewTag(reviewCategory = reviewCategory2, displayText = "tagText3", seq = 100)
        reviewTagRepository.save(reviewTag1)
        reviewTagRepository.save(reviewTag2)
        reviewTagRepository.save(reviewTag3)

        val reviews = (0 until 5).map { i ->
            val offsetDays = i.toLong()

            val chatMessage = ChatMessage(
                member = member1,
                chatRoom = chatRoom,
                type = ChatMessageType.WORKOUT_REQUEST,
                content = "content $i",
                sentAt = time.nowLocalDateTime.plusDays(offsetDays)
            )
            chatMessageRepository.save(chatMessage)

            val workoutRequest = WorkoutRequest.of(
                chatMessage = chatMessage,
                fromMember = member1,
                toMember = member2,
                location = "location",
                scheduledAt = time.nowLocalDateTime.plusDays(offsetDays + 1),
                requestedAt = time.nowLocalDateTime.plusDays(offsetDays)
            )
            workoutRequestRepository.save(workoutRequest)

            val workoutHistory = WorkoutHistory(
                chatRoom = chatRoom,
                workoutRequest = workoutRequest,
                memberOne = member1,
                memberTwo = member2,
                completedAt = time.nowLocalDateTime.plusDays(offsetDays + 1)
            )
            workoutHistoryRepository.save(workoutHistory)

            val review = Review(
                fromMember = member1,
                toMember = member2,
                workoutHistory = workoutHistory,
                isPrivate = false,
                type = ReviewType.GOOD,
                score = 2.0 + (i * 0.5),
                content = "content $i",
                postedAt = time.nowLocalDateTime.plusDays(offsetDays + 2)
            )
            reviewRepository.save(review)
        }

        // when
        val response = workoutRewardQueryService.findWorkoutRewardCouponStatus(
            memberId = member1.id!!
        )

        // then
        assertThat(response)
            .extracting(
                "firstCouponStatus",
                "secondCouponStatus"
            )
            .contains(
                WorkoutRewardCouponStatusResponse.CouponStatus.CLAIMABLE,
                WorkoutRewardCouponStatusResponse.CouponStatus.CLAIMABLE
            )
    }

    @DisplayName("리워드 보상 쿠폰 상태 조회 - 3회에 대한 쿠폰만 지급 가능한 상태")
    @Test
    fun findWorkoutRewardCouponStatusFirstClaimable() {
        // given
        val member1 = Member(
            email = "email1",
            password = "password",
            role = Role.USER
        )
        val member2 = Member(
            email = "email2",
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

        val reviewTag1 = ReviewTag(reviewCategory = reviewCategory1, displayText = "tagText1", seq = 100)
        val reviewTag2 = ReviewTag(reviewCategory = reviewCategory1, displayText = "tagText2", seq = 200)
        val reviewTag3 = ReviewTag(reviewCategory = reviewCategory2, displayText = "tagText3", seq = 100)
        reviewTagRepository.save(reviewTag1)
        reviewTagRepository.save(reviewTag2)
        reviewTagRepository.save(reviewTag3)

        val reviews = (0 until 3).map { i ->
            val offsetDays = i.toLong()

            val chatMessage = ChatMessage(
                member = member1,
                chatRoom = chatRoom,
                type = ChatMessageType.WORKOUT_REQUEST,
                content = "content $i",
                sentAt = time.nowLocalDateTime.plusDays(offsetDays)
            )
            chatMessageRepository.save(chatMessage)

            val workoutRequest = WorkoutRequest.of(
                chatMessage = chatMessage,
                fromMember = member1,
                toMember = member2,
                location = "location",
                scheduledAt = time.nowLocalDateTime.plusDays(offsetDays + 1),
                requestedAt = time.nowLocalDateTime.plusDays(offsetDays)
            )
            workoutRequestRepository.save(workoutRequest)

            val workoutHistory = WorkoutHistory(
                chatRoom = chatRoom,
                workoutRequest = workoutRequest,
                memberOne = member1,
                memberTwo = member2,
                completedAt = time.nowLocalDateTime.plusDays(offsetDays + 1)
            )
            workoutHistoryRepository.save(workoutHistory)

            val review = Review(
                fromMember = member1,
                toMember = member2,
                workoutHistory = workoutHistory,
                isPrivate = false,
                type = ReviewType.GOOD,
                score = 2.0 + (i * 0.5),
                content = "content $i",
                postedAt = time.nowLocalDateTime.plusDays(offsetDays + 2)
            )
            reviewRepository.save(review)
        }

        // when
        val response = workoutRewardQueryService.findWorkoutRewardCouponStatus(
            memberId = member1.id!!
        )

        // then
        assertThat(response)
            .extracting(
                "firstCouponStatus",
                "secondCouponStatus"
            )
            .contains(
                WorkoutRewardCouponStatusResponse.CouponStatus.CLAIMABLE,
                WorkoutRewardCouponStatusResponse.CouponStatus.UNAVAILABLE
            )
    }

    @DisplayName("리워드 보상 쿠폰 상태 조회 - 이미 신청한 상태")
    @Test
    fun findWorkoutRewardCouponStatusAllClaimed() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(member)

        val workoutRewardClaim = WorkoutRewardClaim(
            member = member,
            phoneNumber = "01011111111",
            workoutCount = WorkoutRewardClaimWorkoutCount.FIRST,
            isPrivacyAgreed = true,
            couponStatus = WorkoutRewardClaimCouponStatus.PENDING
        )
        workoutRewardClaimRepository.save(workoutRewardClaim)

        // when
        val response = workoutRewardQueryService.findWorkoutRewardCouponStatus(
            memberId = member.id!!
        )

        // then
        assertThat(response)
            .extracting(
                "firstCouponStatus",
                "secondCouponStatus"
            )
            .contains(
                WorkoutRewardCouponStatusResponse.CouponStatus.CLAIMED,
                WorkoutRewardCouponStatusResponse.CouponStatus.UNAVAILABLE
            )
    }

    @DisplayName("리워드 보상 쿠폰 상태 조회 - 조건 미충족으로 쿠폰 지급을 할 수 없는 상태")
    @Test
    fun findWorkoutRewardCouponStatusAllUnavailable() {
        // given
        val member = Member(
            email = "email",
            password = "password",
            role = Role.USER
        )
        memberRepository.save(member)

        // when
        val response = workoutRewardQueryService.findWorkoutRewardCouponStatus(
            memberId = member.id!!
        )

        // then
        assertThat(response)
            .extracting(
                "firstCouponStatus",
                "secondCouponStatus"
            )
            .contains(
                WorkoutRewardCouponStatusResponse.CouponStatus.UNAVAILABLE,
                WorkoutRewardCouponStatusResponse.CouponStatus.UNAVAILABLE
            )
    }

    @DisplayName("리워드 보상 쿠폰 상태 조회 - 조건 미충족으로 1개 쿠폰 지급을 할 수 없는 상태 + 1개는 이미 신청한 상태")
    @Test
    fun findWorkoutRewardCouponStatusOneClaimedOneUnavailable() {
        // given
        val member1 = Member(
            email = "email1",
            password = "password",
            role = Role.USER
        )
        val member2 = Member(
            email = "email2",
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

        val reviewTag1 = ReviewTag(reviewCategory = reviewCategory1, displayText = "tagText1", seq = 100)
        val reviewTag2 = ReviewTag(reviewCategory = reviewCategory1, displayText = "tagText2", seq = 200)
        val reviewTag3 = ReviewTag(reviewCategory = reviewCategory2, displayText = "tagText3", seq = 100)
        reviewTagRepository.save(reviewTag1)
        reviewTagRepository.save(reviewTag2)
        reviewTagRepository.save(reviewTag3)

        val reviews = (0 until 3).map { i ->
            val offsetDays = i.toLong()

            val chatMessage = ChatMessage(
                member = member1,
                chatRoom = chatRoom,
                type = ChatMessageType.WORKOUT_REQUEST,
                content = "content $i",
                sentAt = time.nowLocalDateTime.plusDays(offsetDays)
            )
            chatMessageRepository.save(chatMessage)

            val workoutRequest = WorkoutRequest.of(
                chatMessage = chatMessage,
                fromMember = member1,
                toMember = member2,
                location = "location",
                scheduledAt = time.nowLocalDateTime.plusDays(offsetDays + 1),
                requestedAt = time.nowLocalDateTime.plusDays(offsetDays)
            )
            workoutRequestRepository.save(workoutRequest)

            val workoutHistory = WorkoutHistory(
                chatRoom = chatRoom,
                workoutRequest = workoutRequest,
                memberOne = member1,
                memberTwo = member2,
                completedAt = time.nowLocalDateTime.plusDays(offsetDays + 1)
            )
            workoutHistoryRepository.save(workoutHistory)

            val review = Review(
                fromMember = member1,
                toMember = member2,
                workoutHistory = workoutHistory,
                isPrivate = false,
                type = ReviewType.GOOD,
                score = 2.0 + (i * 0.5),
                content = "content $i",
                postedAt = time.nowLocalDateTime.plusDays(offsetDays + 2)
            )
            reviewRepository.save(review)
        }

        val workoutRewardClaim = WorkoutRewardClaim(
            member = member1,
            phoneNumber = "01011111111",
            workoutCount = WorkoutRewardClaimWorkoutCount.FIRST,
            isPrivacyAgreed = true,
            couponStatus = WorkoutRewardClaimCouponStatus.PENDING
        )
        workoutRewardClaimRepository.save(workoutRewardClaim)

        // when
        val response = workoutRewardQueryService.findWorkoutRewardCouponStatus(
            memberId = member1.id!!
        )

        // then
        assertThat(response)
            .extracting(
                "firstCouponStatus",
                "secondCouponStatus"
            )
            .contains(
                WorkoutRewardCouponStatusResponse.CouponStatus.CLAIMED,
                WorkoutRewardCouponStatusResponse.CouponStatus.UNAVAILABLE
            )
    }




}