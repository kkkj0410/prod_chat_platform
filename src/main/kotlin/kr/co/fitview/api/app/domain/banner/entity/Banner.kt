package kr.co.fitview.api.app.domain.banner.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.fitview.api.app.domain.banner.entity.enums.BannerDisplayStatus
import kr.co.fitview.api.app.domain.banner.entity.enums.BannerType
import kr.co.fitview.api.app.domain.image.entity.Image
import kr.co.fitview.api.app.global.entity.BaseAuditEntity
import org.hibernate.annotations.ColumnDefault
import java.time.Instant

@Entity
@Table(name = "banner")
open class Banner(

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "image_id", nullable = false)
    open var image: Image? = null,

    @Size(max = 50)
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "banner_type", nullable = false, length = 50)
    open var bannerType: BannerType? = null,

    @Size(max = 50)
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "display_status", nullable = false, length = 50)
    open var displayStatus: BannerDisplayStatus? = null

) : BaseAuditEntity(){
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "banner_id", nullable = false)
    open var id: Long? = null

    fun getImageUrl() : String {
        return image!!.url!!
    }

}