package kr.co.fitview.api.app.domain.workout_reward.repository

import kr.co.fitview.api.app.IntegrationTestSupport
import kr.co.fitview.api.app.domain.chat.repository.ChatMessageRepository
import kr.co.fitview.api.app.domain.chat.repository.ChatRoomRepository
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.member.repository.MemberRepository
import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.domain.review.repository.ReviewCategoryRepository
import kr.co.fitview.api.app.domain.review.repository.ReviewRepository
import kr.co.fitview.api.app.domain.review.repository.ReviewTagRepository
import kr.co.fitview.api.app.domain.workout.repository.WorkoutRequestRepository
import kr.co.fitview.api.app.domain.workout_history.repository.WorkoutHistoryRepository
import kr.co.fitview.api.app.domain.workout_partner.repository.WorkoutPartnerRepository
import kr.co.fitview.api.app.domain.workout_reward.condition.AdminWorkoutRewardCondition
import kr.co.fitview.api.app.domain.workout_reward.entity.WorkoutRewardClaim
import kr.co.fitview.api.app.domain.workout_reward.entity.enums.WorkoutRewardClaimCouponStatus
import kr.co.fitview.api.app.domain.workout_reward.entity.enums.WorkoutRewardClaimCouponType
import kr.co.fitview.api.app.domain.workout_reward.entity.enums.WorkoutRewardClaimWorkoutCount
import kr.co.fitview.api.app.domain.workout_reward.service.WorkoutRewardQueryService
import kr.co.fitview.api.app.global.entity.Role
import kr.co.fitview.api.app.global.time.Time
import kr.co.fitview.api.app.global.util.TestDataFactory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.ZoneId
import java.time.ZoneOffset

class WorkoutRewardClaimRepositoryTest @Autowired constructor(
    private val memberRepository : MemberRepository,
    private val workoutRewardClaimRepository: WorkoutRewardClaimRepository,
    private val oAuth2Service : OAuth2Service,
    private val time : Time
) : IntegrationTestSupport(){


    @DisplayName("운동 리워드 보상 요청 이력을 조회한다.")
    @Test
    fun findAllWorkoutRewardBy() {
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

        val workoutRewardClaim1 = WorkoutRewardClaim(
            member = member1,
            phoneNumber = "01011111111",
            workoutCount = WorkoutRewardClaimWorkoutCount.FIRST,
            isPrivacyAgreed = true,
            couponStatus = WorkoutRewardClaimCouponStatus.PENDING,
            couponType = WorkoutRewardClaimCouponType.BAEMIN
        )
        workoutRewardClaim1.createdAt = time.nowLocalDateTime.plusSeconds(1)
        workoutRewardClaimRepository.save(workoutRewardClaim1)


        val workoutRewardClaim2 = WorkoutRewardClaim(
            member = member1,
            phoneNumber = "01011111111",
            workoutCount = WorkoutRewardClaimWorkoutCount.SECOND,
            isPrivacyAgreed = true,
            couponStatus = WorkoutRewardClaimCouponStatus.PENDING,
            couponType = WorkoutRewardClaimCouponType.BAEMIN
        )
        workoutRewardClaim2.createdAt = time.nowLocalDateTime.plusSeconds(2)
        workoutRewardClaimRepository.save(workoutRewardClaim2)

        val workoutRewardClaim3 = WorkoutRewardClaim(
            member = member2,
            phoneNumber = "01011111111",
            workoutCount = WorkoutRewardClaimWorkoutCount.FIRST,
            isPrivacyAgreed = true,
            couponStatus = WorkoutRewardClaimCouponStatus.PENDING,
            couponType = WorkoutRewardClaimCouponType.BAEMIN
        )
        workoutRewardClaim3.createdAt = time.nowLocalDateTime.plusSeconds(3)
        workoutRewardClaimRepository.save(workoutRewardClaim3)

        val condition = AdminWorkoutRewardCondition(
            size = 10,
            cursorAt = null
        )

        // when
        val response = workoutRewardClaimRepository.findAllWorkoutRewardBy(
            condition = condition
        )

        // then
        assertThat(response.content)
            .extracting("id", "workoutCount", "couponStatus", "couponType")
            .containsExactly(
                tuple(workoutRewardClaim3.id!!, WorkoutRewardClaimWorkoutCount.FIRST, WorkoutRewardClaimCouponStatus.PENDING, WorkoutRewardClaimCouponType.BAEMIN),
                tuple(workoutRewardClaim2.id!!, WorkoutRewardClaimWorkoutCount.SECOND, WorkoutRewardClaimCouponStatus.PENDING, WorkoutRewardClaimCouponType.BAEMIN),
                tuple(workoutRewardClaim1.id!!, WorkoutRewardClaimWorkoutCount.FIRST, WorkoutRewardClaimCouponStatus.PENDING, WorkoutRewardClaimCouponType.BAEMIN)
            )
    }

    @DisplayName("운동 리워드 보상 요청 이력을 조회한다. - cursorAt으로 커서 페이징")
    @Test
    fun findAllWorkoutRewardByCursor() {
        // given
        val member1 = Member(email = "email1", password = "password", role = Role.USER)
        memberRepository.save(member1)
        oAuth2Service.signup(TestDataFactory.oAuth2SignupRequest(nickname = "nickname1", profileImageUrl = "profile1"), member1.id!!)

        val workoutRewardClaim1 = WorkoutRewardClaim(
            member = member1,
            phoneNumber = "01011111111",
            workoutCount = WorkoutRewardClaimWorkoutCount.FIRST,
            isPrivacyAgreed = true,
            couponStatus = WorkoutRewardClaimCouponStatus.PENDING,
            couponType = WorkoutRewardClaimCouponType.BAEMIN
        )
        workoutRewardClaim1.createdAt = time.nowLocalDateTime.plusSeconds(1)
        workoutRewardClaimRepository.save(workoutRewardClaim1)

        val workoutRewardClaim2 = WorkoutRewardClaim(
            member = member1,
            phoneNumber = "01011111111",
            workoutCount = WorkoutRewardClaimWorkoutCount.SECOND,
            isPrivacyAgreed = true,
            couponStatus = WorkoutRewardClaimCouponStatus.PENDING,
            couponType = WorkoutRewardClaimCouponType.BAEMIN
        )
        workoutRewardClaim2.createdAt = time.nowLocalDateTime.plusSeconds(2)
        workoutRewardClaimRepository.save(workoutRewardClaim2)

        val workoutRewardClaim3 = WorkoutRewardClaim(
            member = member1,
            phoneNumber = "01011111111",
            workoutCount = WorkoutRewardClaimWorkoutCount.FIRST,
            isPrivacyAgreed = true,
            couponStatus = WorkoutRewardClaimCouponStatus.PENDING,
            couponType = WorkoutRewardClaimCouponType.BAEMIN
        )
        workoutRewardClaim3.createdAt = time.nowLocalDateTime.plusSeconds(3)
        workoutRewardClaimRepository.save(workoutRewardClaim3)

        val condition = AdminWorkoutRewardCondition(
            size = 10,
            cursorAt = workoutRewardClaim3.createdAt!!
                .atZone(ZoneId.of("Asia/Seoul"))
                .toInstant()
                .toEpochMilli()
        )

        // when
        val response = workoutRewardClaimRepository.findAllWorkoutRewardBy(condition = condition)

        // then
        assertThat(response.content)
            .extracting("id")
            .containsExactly(
                workoutRewardClaim2.id!!,
                workoutRewardClaim1.id!!
            )
    }

    @DisplayName("운동 리워드 보상 요청 이력을 조회한다. - size로 개수 제한")
    @Test
    fun findAllWorkoutRewardBySize() {
        // given
        val member1 = Member(email = "email1", password = "password", role = Role.USER)
        memberRepository.save(member1)
        oAuth2Service.signup(TestDataFactory.oAuth2SignupRequest(nickname = "nickname1", profileImageUrl = "profile1"), member1.id!!)

        val workoutRewardClaim1 = WorkoutRewardClaim(
            member = member1,
            phoneNumber = "01011111111",
            workoutCount = WorkoutRewardClaimWorkoutCount.FIRST,
            isPrivacyAgreed = true,
            couponStatus = WorkoutRewardClaimCouponStatus.PENDING,
            couponType = WorkoutRewardClaimCouponType.BAEMIN
        )
        workoutRewardClaim1.createdAt = time.nowLocalDateTime.plusSeconds(1)
        workoutRewardClaimRepository.save(workoutRewardClaim1)

        val workoutRewardClaim2 = WorkoutRewardClaim(
            member = member1,
            phoneNumber = "01011111111",
            workoutCount = WorkoutRewardClaimWorkoutCount.SECOND,
            isPrivacyAgreed = true,
            couponStatus = WorkoutRewardClaimCouponStatus.PENDING,
            couponType = WorkoutRewardClaimCouponType.BAEMIN
        )
        workoutRewardClaim2.createdAt = time.nowLocalDateTime.plusSeconds(2)
        workoutRewardClaimRepository.save(workoutRewardClaim2)

        val workoutRewardClaim3 = WorkoutRewardClaim(
            member = member1,
            phoneNumber = "01011111111",
            workoutCount = WorkoutRewardClaimWorkoutCount.FIRST,
            isPrivacyAgreed = true,
            couponStatus = WorkoutRewardClaimCouponStatus.PENDING,
            couponType = WorkoutRewardClaimCouponType.BAEMIN
        )
        workoutRewardClaim3.createdAt = time.nowLocalDateTime.plusSeconds(3)
        workoutRewardClaimRepository.save(workoutRewardClaim3)

        val condition = AdminWorkoutRewardCondition(
            size = 2,
            cursorAt = null
        )

        // when
        val response = workoutRewardClaimRepository.findAllWorkoutRewardBy(condition = condition)

        // then
        assertThat(response.content).hasSize(2)
        assertThat(response.content)
            .extracting("id")
            .containsExactly(
                workoutRewardClaim3.id!!,
                workoutRewardClaim2.id!!
            )
    }



}