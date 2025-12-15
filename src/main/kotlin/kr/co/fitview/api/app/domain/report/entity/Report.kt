package kr.co.fitview.api.app.domain.report.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.fitview.api.app.domain.member.entity.Member
import kr.co.fitview.api.app.domain.report.entity.enums.ReportTargetType
import kr.co.fitview.api.app.global.entity.BaseSoftDeleteEntity
import org.hibernate.annotations.ColumnDefault
import java.time.Instant
import java.time.LocalDateTime

@Entity
@Table(name = "report")
class Report(

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    var member: Member? = null,

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_reason_id", nullable = false)
    var reportReason: ReportReason? = null,

    @Size(max = 100)
    @NotNull
    @Column(name = "target_type", nullable = false, length = 100)
    @Enumerated(EnumType.STRING)
    var targetType: ReportTargetType? = null,

    @Lob
    @Column(name = "description")
    var description: String? = null,

    @NotNull
    @Column(name = "reported_at", nullable = false)
    var reportedAt: LocalDateTime? = null

) : BaseSoftDeleteEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id", nullable = false)
    var id: Long? = null

    @OneToMany(mappedBy = "report")
    var chatRoomReports: MutableSet<ChatRoomReport> = mutableSetOf()

    @OneToMany(mappedBy = "report")
    var memberReports: MutableSet<MemberReport> = mutableSetOf()
}