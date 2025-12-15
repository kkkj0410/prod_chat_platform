package kr.co.fitview.api.app.domain.report.repository

import kr.co.fitview.api.app.domain.report.entity.ReportReason
import kr.co.fitview.api.app.domain.report.entity.enums.ReportTargetType
import org.springframework.data.jpa.repository.JpaRepository

interface ReportReasonRepository : JpaRepository<ReportReason, Long> {

    fun findAllByTargetTypeAndDeletedAtIsNullOrderBySeqAsc(type: ReportTargetType): List<ReportReason>
}