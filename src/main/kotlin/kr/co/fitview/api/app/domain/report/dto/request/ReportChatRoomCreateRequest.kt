package kr.co.fitview.api.app.domain.report.dto.request

import jakarta.validation.constraints.NotNull
import kr.co.fitview.api.app.domain.report.entity.enums.ReportReasonType

data class ReportChatRoomCreateRequest(

    @field:NotNull(message = "chatRoomId is required")
    val chatRoomId : Long?,

    @field:NotNull(message = "reasonType is required")
    val reasonType : ReportReasonType?,

    val description : String?
)
