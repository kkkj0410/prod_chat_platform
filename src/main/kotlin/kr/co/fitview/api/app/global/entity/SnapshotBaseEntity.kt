package kr.co.fitview.api.app.global.entity

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import kr.co.fitview.api.app.global.time.TimeHolder
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime


@MappedSuperclass
open class SnapshotBaseEntity{

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null

    @CreatedDate
    @Column(name = "snapshot_created_at", nullable = false, updatable = false)
    var snapshotCreatedAt: LocalDateTime? = null

    @LastModifiedDate
    @Column(name = "snapshot_updated_at", nullable = false)
    var snapshotUpdatedAt: LocalDateTime? = null


    @PrePersist
    fun prePersist() {
        val now = TimeHolder.time.nowLocalDateTime

        if (snapshotCreatedAt == null) {
            snapshotCreatedAt = now
        }
        if (snapshotUpdatedAt == null) {
            snapshotUpdatedAt = now
        }
    }

    @PreUpdate
    fun preUpdate() {
        snapshotUpdatedAt = TimeHolder.time.nowLocalDateTime
    }
}