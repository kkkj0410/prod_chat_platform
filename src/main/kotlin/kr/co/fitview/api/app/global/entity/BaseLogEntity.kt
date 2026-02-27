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
open class BaseLogEntity{

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null


    @PrePersist
    fun prePersist() {
        val now = TimeHolder.time.nowLocalDateTime
        if (createdAt == null) {
            createdAt = now
        }
    }

}