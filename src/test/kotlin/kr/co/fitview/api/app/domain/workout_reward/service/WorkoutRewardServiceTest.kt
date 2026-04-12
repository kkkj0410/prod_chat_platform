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
import kr.co.fitview.api.app.domain.review.repository.ReviewTagRepository
import kr.co.fitview.api.app.domain.workout.entity.WorkoutRequest
import kr.co.fitview.api.app.domain.workout.repository.WorkoutRequestRepository
import kr.co.fitview.api.app.domain.workout_history.entity.WorkoutHistory
import kr.co.fitview.api.app.domain.workout_history.repository.WorkoutHistoryRepository
import kr.co.fitview.api.app.domain.workout_partner.entity.WorkoutPartner
import kr.co.fitview.api.app.domain.workout_partner.repository.WorkoutPartnerRepository
import kr.co.fitview.api.app.domain.workout_reward.dto.request.WorkoutRewardClaimServiceRequest
import kr.co.fitview.api.app.domain.workout_reward.entity.WorkoutRewardClaim
import kr.co.fitview.api.app.domain.workout_reward.entity.enums.WorkoutRewardClaimCouponStatus
import kr.co.fitview.api.app.domain.workout_reward.entity.enums.WorkoutRewardClaimCouponType
import kr.co.fitview.api.app.domain.workout_reward.entity.enums.WorkoutRewardClaimWorkoutCount
import kr.co.fitview.api.app.domain.workout_reward.repository.WorkoutRewardClaimRepository
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.exception.GlobalException
import kr.co.fitview.api.app.global.exception.error.workout_partner.WorkoutPartnerErrorCode
import kr.co.fitview.api.app.global.exception.workout_reward.WorkoutRewardErrorCode
import kr.co.fitview.api.app.global.time.Time
import kr.co.fitview.api.app.global.util.TestDataFactory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.ThrowingConsumer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired

class WorkoutRewardServiceTest @Autowired constructor(
    private val workoutRewardClaimRepository : WorkoutRewardClaimRepository,
    private val workoutRewardService : WorkoutRewardService,
    private val reviewCategoryRepository: ReviewCategoryRepository,
    private val reviewTagRepository : ReviewTagRepository,
    private val memberRepository : MemberRepository,
    private val chatRoomRepository : ChatRoomRepository,
    private val workoutHistoryRepository : WorkoutHistoryRepository,
    private val reviewRepository : ReviewRepository,
    private val chatMessageRepository : ChatMessageRepository,
    private val workoutRequestRepository : WorkoutRequestRepository,
    private val workoutPartnerRepository : WorkoutPartnerRepository,
    private val oAuth2Service : OAuth2Service,
    private val time : Time
) : IntegrationTestSupport(){

    @DisplayName("운동 리워드 보상을 요청한다.")
    @Test
    fun addWorkoutRewardClaim() {
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


        val request = WorkoutRewardClaimServiceRequest(
            phoneNumber = "01011111111",
            workoutRewardCouponType = WorkoutRewardClaimCouponType.BAEMIN,
            workoutRewardCouponLevel = WorkoutRewardClaimWorkoutCount.FIRST
        )

        // when
        val savedWorkoutReward = workoutRewardService.addWorkoutRewardClaim(
            memberId = member1.id!!,
            request = request
        )

        // then
        assertThat(savedWorkoutReward.id).isNotNull()
        assertThat(savedWorkoutReward)
            .extracting(
                "member.id",
                "phoneNumber",
                "workoutCount",
                "isPrivacyAgreed",
                "couponStatus",
                "couponType"
            )
            .contains(
                member1.id,
                request.phoneNumber,
                WorkoutRewardClaimWorkoutCount.FIRST,
                true,
                WorkoutRewardClaimCouponStatus.PENDING,
                WorkoutRewardClaimCouponType.BAEMIN
            )
    }

    @DisplayName("이미 운동 리워드 보상을 요청했으면 다시 요청을 보내지 못한다.")
    @ParameterizedTest(name = "보상 단계: {0}")
    @CsvSource(
        "FIRST",
        "SECOND"
    )
    fun addWorkoutRewardClaimAlreadyExistsClaim(
        workoutCount: WorkoutRewardClaimWorkoutCount
    ) {
        // given
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

        val workoutRewardClaim = WorkoutRewardClaim(
            member = member1,
            phoneNumber = "01000000000",
            workoutCount = workoutCount,
            isPrivacyAgreed = true,
            couponStatus = WorkoutRewardClaimCouponStatus.PENDING,
            couponType = WorkoutRewardClaimCouponType.BAEMIN,
        )
        workoutRewardClaimRepository.save(workoutRewardClaim)

        val request = WorkoutRewardClaimServiceRequest(
            phoneNumber = "01011111111",
            workoutRewardCouponType = WorkoutRewardClaimCouponType.NAVER_PAY,
            workoutRewardCouponLevel = workoutCount
        )

        // when & then
        assertThatThrownBy {
            workoutRewardService.addWorkoutRewardClaim(
                memberId = member1.id!!,
                request = request
            )
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(WorkoutRewardErrorCode.ALREADY_COUPON_REQUESTED)
            })
    }

    @DisplayName("운동 리워드 보상 요청 조건을 달성하지 못하면 요청을 보내지 못한다 - 스탬프 3회 조건")
    @Test
    fun addWorkoutRewardClaimConditionWhenStamp3() {
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

        val reviews = (0 until 2).map { i ->
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


        val request = WorkoutRewardClaimServiceRequest(
            phoneNumber = "01011111111",
            workoutRewardCouponType = WorkoutRewardClaimCouponType.NAVER_PAY,
            workoutRewardCouponLevel = WorkoutRewardClaimWorkoutCount.FIRST
        )

        // when & then
        assertThatThrownBy {
            workoutRewardService.addWorkoutRewardClaim(
                memberId = member1.id!!,
                request = request
            )
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(WorkoutRewardErrorCode.COUPON_CONDITION_NOT_MET)
            })
    }

    @DisplayName("운동 리워드 보상 요청 조건을 달성하지 못하면 요청을 보내지 못한다 - 스탬프 5회 조건")
    @Test
    fun addWorkoutRewardClaimConditionWhenStamp5() {
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

        val reviews = (0 until 4).map { i ->
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


        val request = WorkoutRewardClaimServiceRequest(
            phoneNumber = "01011111111",
            workoutRewardCouponType = WorkoutRewardClaimCouponType.NAVER_PAY,
            workoutRewardCouponLevel = WorkoutRewardClaimWorkoutCount.SECOND
        )

        // when & then
        assertThatThrownBy {
            workoutRewardService.addWorkoutRewardClaim(
                memberId = member1.id!!,
                request = request
            )
        }
            .isInstanceOf(GlobalException::class.java)
            .satisfies(ThrowingConsumer { ex ->
                val globalEx = ex as GlobalException
                assertThat(globalEx.errorCode)
                    .isEqualTo(WorkoutRewardErrorCode.COUPON_CONDITION_NOT_MET)
            })
    }

}