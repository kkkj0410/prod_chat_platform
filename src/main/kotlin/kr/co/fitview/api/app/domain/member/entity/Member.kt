package kr.co.fitview.api.app.domain.member.entity

import jakarta.persistence.*
import org.hibernate.annotations.ColumnDefault
import java.time.Instant

@Entity
@Table(name = "member")
class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id", nullable = false)
    var id: Long? = null

    @Column(name = "login_id", nullable = false, length = 100)
    var loginId: String? = null

    @Column(name = "password", nullable = false)
    var password: String? = null

    @Column(name = "email", nullable = false, length = 100)
    var email: String? = null

    @ColumnDefault("'USER'")
    @Column(name = "role", nullable = false, length = 20)
    var role: String? = null

    @Lob
    @Column(name = "provider")
    var provider: String? = null

    @Column(name = "provider_id", length = 100)
    var providerId: String? = null

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant? = null

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant? = null

    @Column(name = "deleted_at")
    var deletedAt: Instant? = null
}