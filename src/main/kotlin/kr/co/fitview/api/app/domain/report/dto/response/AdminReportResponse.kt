package kr.co.fitview.api.app.domain.report.dto.response

import com.fasterxml.jackson.annotation.JsonIgnore
import kr.co.fitview.api.app.domain.report.entity.enums.ReportTargetType
import java.time.LocalDateTime

data class AdminReportResponse (

    @JsonIgnore
    val fromMemberId : Long,
    val reportId : Long,
    val fromMemberNickname : String,
    val toMemberNickname : String,
    val reportReasonDisplayText : String,
    val reportDescription : String?,
    val reportedAt : LocalDateTime,
    val reportTargetType : ReportTargetType
){

    constructor(
        fromMemberId : Long,
        reportId: Long,
        fromMemberNickname: String,
        reportReasonDisplayText: String,
        reportDescription: String?,
        reportedAt: LocalDateTime,
        reportTargetType: ReportTargetType
    ) : this(
        fromMemberId = fromMemberId,
        reportId = reportId,
        fromMemberNickname = fromMemberNickname,
        toMemberNickname = "",
        reportReasonDisplayText = reportReasonDisplayText,
        reportDescription = reportDescription,
        reportedAt = reportedAt,
        reportTargetType = reportTargetType
    )
}