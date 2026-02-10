package kr.co.fitview.api.app.domain.stat.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.fitview.api.app.domain.stat.entity.enums.ApiStatMethod
import kr.co.fitview.api.app.global.entity.BaseEntity
import org.hibernate.annotations.ColumnDefault
import java.time.Instant
import java.time.LocalDate

@Entity
@Table(name = "api_stat")
open class ApiStat(

    @NotNull
    @Column(name = "stat_date", nullable = false)
    open var statDate: LocalDate? = null,

    @Size(max = 20)
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false, length = 20)
    open var method: ApiStatMethod? = null,

    @Size(max = 255)
    @NotNull
    @Column(name = "path", nullable = false)
    open var path: String? = null,

    @NotNull
    @ColumnDefault("0")
    @Column(name = "count", nullable = false)
    open var count: Long? = null

) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "api_stat_id", nullable = false)
    open var id: Long? = null

}