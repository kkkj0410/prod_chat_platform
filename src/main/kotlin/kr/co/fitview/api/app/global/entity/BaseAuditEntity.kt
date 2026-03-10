package kr.co.fitview.api.app.global.entity

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import kr.co.fitview.api.app.global.security.UserPrincipal
import kr.co.fitview.api.app.global.time.TimeHolder
import kr.co.fitview.api.app.global.util.SecurityHolder
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import java.time.LocalDateTime


@MappedSuperclass
open class BaseAuditEntity{

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null

    @Column(name = "created_by", nullable = false, updatable = false, length = 100)
    var createdBy: String? = null
        protected set

    @Column(name = "updated_by", nullable = false, length = 100)
    var updatedBy: String? = null
        protected set

    @PrePersist
    fun prePersist() {
        val now = TimeHolder.time.nowLocalDateTime
        if (createdAt == null) {
            createdAt = now
        }
        if (updatedAt == null) {
            updatedAt = now
        }

        val memberId = SecurityHolder.provider.getMemberIdOrNull()?.toString() ?: "SYSTEM"
        if(createdBy == null){
            createdBy = memberId
        }
        if(updatedBy == null){
            updatedBy = memberId
        }
    }


    @PreUpdate
    fun preUpdate() {
        updatedAt = TimeHolder.time.nowLocalDateTime

        val memberId = SecurityHolder.provider.getMemberIdOrNull()?.toString() ?: "SYSTEM"
        updatedBy = memberId
    }


}