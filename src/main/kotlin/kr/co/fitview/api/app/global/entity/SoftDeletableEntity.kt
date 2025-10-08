package kr.co.fitview.api.app.global.entity

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import java.time.LocalDateTime

@MappedSuperclass
open class SoftDeletableEntity : BaseEntity() {
    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null
}