package kr.co.fitview.api.app.domain.report.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.fitview.api.app.domain.report.entity.enums.ReportReasonType
import kr.co.fitview.api.app.domain.report.entity.enums.ReportTargetType
import kr.co.fitview.api.app.global.entity.BaseSoftDeleteEntity
import org.hibernate.annotations.ColumnDefault
import java.time.Instant

@Entity
@Table(name = "report_reason")
class ReportReason(

    @Size(max = 100)
    @Column(name = "target_type", length = 100)
    @Enumerated(EnumType.STRING)
    var targetType: ReportTargetType? = null,

    @Size(max = 100)
    @NotNull
    @Column(name = "reason_type", nullable = false, length = 100)
    @Enumerated(EnumType.STRING)
    var reasonType: ReportReasonType? = null,

    @Size(max = 200)
    @NotNull
    @Column(name = "display_text", nullable = false, length = 200)
    var displayText: String? = null,

    @NotNull
    @Column(name = "seq", nullable = false)
    var seq: Int? = null

) : BaseSoftDeleteEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_reason_id", nullable = false)
    var id: Long? = null
}