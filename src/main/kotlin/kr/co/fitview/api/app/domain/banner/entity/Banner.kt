package kr.co.fitview.api.app.domain.banner.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.fitview.api.app.domain.image.entity.Image
import org.hibernate.annotations.ColumnDefault
import java.time.Instant

@Entity
@Table(name = "banner")
open class Banner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "banner_id", nullable = false)
    open var id: Long? = null

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "image_id", nullable = false)
    open var image: Image? = null

    @Size(max = 100)
    @NotNull
    @Column(name = "created_by", nullable = false, length = 100)
    open var createdBy: String? = null

    @Size(max = 100)
    @NotNull
    @Column(name = "updated_by", nullable = false, length = 100)
    open var updatedBy: String? = null

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", nullable = false)
    open var createdAt: Instant? = null

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at", nullable = false)
    open var updatedAt: Instant? = null

    @Size(max = 50)
    @NotNull
    @Column(name = "banner_type", nullable = false, length = 50)
    open var bannerType: String? = null

    @Size(max = 50)
    @NotNull
    @Column(name = "display_status", nullable = false, length = 50)
    open var displayStatus: String? = null

}