package kr.co.fitview.api.app.domain.stat.dto.response

data class RetentionResultResponse(
    val retentionRate: Double,
    val signupCount: Int,
    val comebackCount: Long,
)