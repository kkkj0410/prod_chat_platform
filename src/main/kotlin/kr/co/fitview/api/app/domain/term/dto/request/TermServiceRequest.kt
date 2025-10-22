package kr.co.fitview.api.app.domain.term.dto.request

import jakarta.validation.constraints.NotNull
import kr.co.fitview.api.app.domain.term.entity.enums.TermName

data class TermServiceRequest(
    val termName : TermName,
    val isAgreed : Boolean
) {
}