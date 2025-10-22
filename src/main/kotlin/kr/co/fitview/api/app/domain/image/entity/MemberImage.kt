package kr.co.fitview.api.app.domain.image.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.fitview.api.app.domain.image.entity.enums.MemberImageType
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.global.entity.BaseEntity
import org.hibernate.annotations.ColumnDefault
import java.time.Instant
import java.time.LocalDateTime

@Entity
@Table(name = "member_image")
class MemberImage(

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    var member: Member? = null,

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "image_id", nullable = false)
    var image: Image? = null,

    @Size(max = 100)
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 100)
    var type: MemberImageType? = null,

    @Column(name = "seq")
    var seq: Int? = null

)  : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_image_id", nullable = false)
    var id: Long? = null

    fun changeImageUrl(url : String) : MemberImage{
        this.image?.changeUrl(url)
        return this
    }

    fun delete(now : LocalDateTime) : MemberImage{
        this.image?.delete(now)
        this.deletedAt = now
        return this
    }

}