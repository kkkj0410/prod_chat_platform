package kr.co.fitview.api.app.domain.member.entity

import jakarta.persistence.*
import kr.co.fitview.api.app.global.entity.BaseEntity
import kr.co.fitview.api.app.global.entity.Role

@Entity
@Table(name = "member")
class Member(

    @Column(name = "email", nullable = false, length = 100)
    var email: String? = null,

    @Column(name = "password", nullable = false)
    var password: String? = null,


    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    var role: Role? = Role.USER,

    @Lob
    @Column(name = "provider")
    var provider: String? = null,

    @Column(name = "provider_id", length = 100)
    var providerId: String? = null,

    ) : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id", nullable = false)
    var id: Long? = null
}