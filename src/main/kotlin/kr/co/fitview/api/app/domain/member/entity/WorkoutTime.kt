package kr.co.fitview.api.app.domain.member.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.fitview.api.app.domain.member.entity.enums.WorkoutTimeName
import kr.co.fitview.api.app.global.entity.BaseSoftDeleteEntity
import java.time.LocalDateTime

@Entity
@Table(name = "workout_time")
class WorkoutTime(

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    var member: Member? = null,

    @Size(max = 100)
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, length = 100)
    var name: WorkoutTimeName? = null

) : BaseSoftDeleteEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "workout_time_id", nullable = false)
    var id: Long? = null

    fun delete(now : LocalDateTime) : WorkoutTime{
        this.deletedAt = now
        return this
    }
}