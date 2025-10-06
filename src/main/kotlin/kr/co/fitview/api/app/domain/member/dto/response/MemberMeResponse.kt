package kr.co.fitview.api.app.domain.member.dto.response

import kr.co.fitview.api.app.global.entity.Role

data class MemberMeResponse(

    val loginId : String,
    val role : Role
)
