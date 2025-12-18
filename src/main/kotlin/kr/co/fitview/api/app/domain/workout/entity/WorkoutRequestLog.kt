package kr.co.fitview.api.app.domain.workout.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import kr.co.fitview.api.app.global.entity.BaseEntity
import java.time.LocalDateTime

@Entity
@Table(name = "workout_request_log")
class WorkoutRequestLog(

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workout_request_id", nullable = false)
    var workoutRequest: WorkoutRequest? = null,

    @Size(max = 50)
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    var status: WorkoutRequestStatus? = null,

    @NotNull
    @Column(name = "logged_at", nullable = false)
    var loggedAt: LocalDateTime? = null


) : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "workout_request_log_id", nullable = false)
    var id: Long? = null

}