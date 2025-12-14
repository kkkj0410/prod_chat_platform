package kr.co.fitview.api.app.domain.report.dto.response

import kr.co.fitview.api.app.domain.report.entity.enums.ReportTargetType
import java.time.LocalDateTime

data class AdminReportResponse (
    val reportId : Long,
    val fromMemberNickname : String,
    val toMemberNickname : String,
    val reportReasonDisplayText : String,
    val reportDescription : String?,
    val reportedAt : LocalDateTime,
    val reportTargetType : ReportTargetType
)