package kr.co.fitview.api.app.domain.member.dto.response

data class MemberAddressResponse(
    val addressId : Long,
    val siDo: String,
    val siGunGu: String,
    val eupMyeonDong: String
)
