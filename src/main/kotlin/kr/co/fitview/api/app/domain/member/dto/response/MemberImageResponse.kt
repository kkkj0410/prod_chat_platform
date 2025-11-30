package kr.co.fitview.api.app.domain.member.dto.response

data class MemberImageResponse(
    val memberId : Long,
    val imageUrl : String,
    val seq : Int
)
