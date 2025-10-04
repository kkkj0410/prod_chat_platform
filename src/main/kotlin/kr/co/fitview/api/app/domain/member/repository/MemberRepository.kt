package kr.co.fitview.api.app.domain.member.repository

import kr.co.fitview.api.app.domain.member.entity.Member
import org.springframework.data.jpa.repository.JpaRepository

interface MemberRepository : JpaRepository<Member, Long> {

    fun findByLoginIdAndDeletedAtIsNull(loginId : String) : Member?

    fun findByLoginIdAndPasswordAndDeletedAtIsNull(loginId : String, password : String) : Member?
}