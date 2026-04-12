package kr.co.fitview.api.app.domain.workout_reward.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.workout_reward.converter.WorkoutRewardClaimWorkoutCountConverter
import kr.co.fitview.api.app.domain.workout_reward.entity.enums.WorkoutRewardClaimCouponStatus
import kr.co.fitview.api.app.domain.workout_reward.entity.enums.WorkoutRewardClaimCouponType
import kr.co.fitview.api.app.domain.workout_reward.entity.enums.WorkoutRewardClaimWorkoutCount
import kr.co.fitview.api.app.global.entity.BaseAuditEntity
import kr.co.fitview.api.app.global.entity.BaseEntity
import java.time.Instant

@Entity
@Table(name = "workout_reward_claim")
open class WorkoutRewardClaim(

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    open var member: Member? = null,

    @Size(max = 50)
    @NotNull
    @Column(name = "phone_number", nullable = false, length = 50)
    open var phoneNumber: String? = null,

    @NotNull
    @Convert(converter = WorkoutRewardClaimWorkoutCountConverter::class)
    @Column(name = "workout_count", nullable = false)
    open var workoutCount: WorkoutRewardClaimWorkoutCount? = null,

    @NotNull
    @Column(name = "is_privacy_agreed", nullable = false)
    open var isPrivacyAgreed: Boolean? = null,

    @Size(max = 50)
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "coupon_status", nullable = false, length = 50)
    open var couponStatus: WorkoutRewardClaimCouponStatus? = null,

    @Size(max = 30)
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "coupon_type", nullable = false, length = 30)
    open var couponType: WorkoutRewardClaimCouponType? = null,

    ) : BaseAuditEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "workout_reward_claim_id", nullable = false)
    open var id: Long? = null


}