package kr.co.fitview.api.app.domain.report.dto.request

import jakarta.validation.constraints.NotNull
import kr.co.fitview.api.app.domain.report.entity.enums.ReportReasonType
import kr.co.fitview.api.app.domain.review.dto.request.ReviewCreateServiceRequest

data class ReportChatRoomCreateRequest(

    @field:NotNull(message = "chatRoomId is required")
    val chatRoomId : Long?,

    @field:NotNull(message = "reportReasonId is required")
    val reportReasonId : Long?,

    val description : String?

){

    fun toServiceRequest() : ReportChatRoomCreateServiceRequest {
        return ReportChatRoomCreateServiceRequest(
            chatRoomId = chatRoomId!!,
            reportReasonId = reportReasonId!!,
            description = description,
        )
    }
}
