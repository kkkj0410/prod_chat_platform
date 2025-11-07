package kr.co.fitview.api.app.domain.member.dto.response

import kr.co.fitview.api.app.global.entity.Role

data class MemberMeResponse(

    val memberId : Long,
    val email : String,
    val role : Role
)
