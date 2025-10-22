package kr.co.fitview.api.app.domain.oauth2.dto.request

import jakarta.validation.constraints.NotNull
import kr.co.fitview.api.app.domain.term.dto.request.TermServiceRequest
import kr.co.fitview.api.app.domain.term.entity.enums.TermName

data class TermRequest(

    @field:NotNull(message = "termName is required")
    val termName : TermName?,

    @field:NotNull(message = "isAgreed is required")
    val isAgreed : Boolean?
) {
    fun toServiceRequest() : TermServiceRequest{
        return TermServiceRequest(
            termName = termName!!,
            isAgreed = isAgreed!!
        )
    }

    companion object {
        fun toServiceRequest(requests: List<TermRequest>): List<TermServiceRequest> {
            return requests.map { it.toServiceRequest()}
        }
    }
}