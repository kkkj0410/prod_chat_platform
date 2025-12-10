package kr.co.fitview.api.app.domain.review.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.fitview.api.app.domain.review.entity.enums.ReviewReminderLogType
import kr.co.fitview.api.app.domain.workout_history.entity.WorkoutHistory
import kr.co.fitview.api.app.global.entity.BaseEntity
import java.time.LocalDateTime

@Entity
@Table(name = "review_reminder_log")
class ReviewReminderLog(

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workout_history_id", nullable = false)
    var workoutHistory: WorkoutHistory? = null,

    @Size(max = 50)
    @NotNull
    @Column(name = "type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    var type: ReviewReminderLogType? = null,

    @NotNull
    @Column(name = "sent_at", nullable = false)
    var sentAt: LocalDateTime? = null

) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_reminder_log_id", nullable = false)
    var id: Long? = null

}