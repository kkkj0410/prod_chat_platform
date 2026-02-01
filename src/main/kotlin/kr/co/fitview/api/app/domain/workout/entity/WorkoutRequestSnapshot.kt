package kr.co.fitview.api.app.domain.workout.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.fitview.api.app.domain.workout.entity.enums.WorkoutRequestStatus
import kr.co.fitview.api.app.global.entity.SnapshotBaseEntity
import java.time.LocalDateTime

@Entity
@Table(name = "workout_request_snapshot")
class WorkoutRequestSnapshot(
    @NotNull
    @Column(name = "workout_request_id", nullable = false)
    var workoutRequestId: Long? = null,

    @NotNull
    @Column(name = "chat_message_id", nullable = false)
    var chatMessageId: Long? = null,

    @NotNull
    @Column(name = "from_member_id", nullable = false)
    var fromMemberId: Long? = null,

    @NotNull
    @Column(name = "to_member_id", nullable = false)
    var toMemberId: Long? = null,

    @Size(max = 50)
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    var status: WorkoutRequestStatus? = null,

    @Size(max = 50)
    @NotNull
    @Column(name = "location", nullable = false, length = 50)
    var location: String? = null,

    @NotNull
    @Column(name = "scheduled_at", nullable = false)
    var scheduledAt: LocalDateTime? = null,

    @NotNull
    @Column(name = "requested_at", nullable = false)
    var requestedAt: LocalDateTime? = null

) : SnapshotBaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "workout_request_snapshot_id", nullable = false)
    var id: Long? = null

    companion object {
        fun ofStatus(workoutRequest: WorkoutRequest, status : WorkoutRequestStatus): WorkoutRequestSnapshot {
            return WorkoutRequestSnapshot(
                workoutRequestId = workoutRequest.id!!,
                chatMessageId = workoutRequest.getChatMessageId(),
                fromMemberId = workoutRequest.getFromMemberId(),
                toMemberId = workoutRequest.getToMemberId(),

                status = status,
                location = workoutRequest.location,
                scheduledAt = workoutRequest.scheduledAt,
                requestedAt = workoutRequest.requestedAt
            ).apply {
                this.createdAt = workoutRequest.createdAt!!
                this.updatedAt = workoutRequest.updatedAt!!
            }
        }
    }
}