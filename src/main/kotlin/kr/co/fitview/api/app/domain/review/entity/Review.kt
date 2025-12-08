package kr.co.fitview.api.app.domain.review.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.review.entity.enums.ReviewType
import kr.co.fitview.api.app.domain.workout_history.entity.WorkoutHistory
import kr.co.fitview.api.app.global.entity.BaseSoftDeleteEntity
import org.hibernate.annotations.ColumnDefault
import java.time.Instant
import java.time.LocalDateTime

@Entity
@Table(name = "review")
class Review(

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_member_id", nullable = false)
    var fromMember: Member? = null,

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_member_id", nullable = false)
    var toMember: Member? = null,

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workout_history_id", nullable = false)
    var workoutHistory: WorkoutHistory? = null,

    @NotNull
    @ColumnDefault("0")
    @Column(name = "is_private", nullable = false)
    var isPrivate: Boolean? = false,

    @Size(max = 20)
    @NotNull
    @Column(name = "type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    var type: ReviewType? = null,

    @NotNull
    @Column(name = "score", nullable = false)
    var score: Double? = null,

    @Size(max = 1000)
    @Column(name = "content", length = 1000)
    var content: String? = null,

    @Column(name = "posted_at", nullable = false)
    var postedAt: LocalDateTime? = null

) : BaseSoftDeleteEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id", nullable = false)
    var id: Long? = null

}