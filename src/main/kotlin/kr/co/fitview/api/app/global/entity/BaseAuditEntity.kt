package kr.co.fitview.api.app.global.entity

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import kr.co.fitview.api.app.global.time.TimeHolder
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime


@MappedSuperclass
open class BaseAuditEntity{

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false, length = 100)
    var createdBy: String? = null
        protected set

    @LastModifiedBy
    @Column(name = "updated_by", nullable = false, length = 100)
    var updatedBy: String? = null
        protected set

    @PrePersist
    fun prePersist() {
        val now = TimeHolder.time.nowLocalDateTime
        if (createdAt == null) {
            createdAt = now
        }
    }


    @PreUpdate
    fun preUpdate() {
        updatedAt = TimeHolder.time.nowLocalDateTime
    }

}